/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.webflux.common.module.workflow.service;

import com.google.common.base.CaseFormat;
import io.jrb.labs.webflux.common.module.workflow.model.ClaimTicket;
import io.jrb.labs.webflux.common.module.workflow.model.WorkflowStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jrb.labs.webflux.common.validation.Validation.required;

@Slf4j
public class WorkflowService implements IWorkflowService {

    private final Map<String, IWorkflowFactory<? extends IWorkflowContext>> workflowFactories;
    private final Map<String, String> workflowAliases;
    private final IWorkflowStateRepository workflowStateRepository;

    public WorkflowService(
            final Map<String, IWorkflowFactory<? extends IWorkflowContext>> workflowFactories,
            final Map<String, String> workflowAliases,
            final IWorkflowStateRepository workflowStateRepository
    ) {
        this.workflowFactories = required(workflowFactories, "workflowFactories")
                .keySet().stream()
                .collect(Collectors.toMap(this::calculateWorkflowName, workflowFactories::get));
        this.workflowAliases = required(workflowAliases, "workflowAliases");
        this.workflowStateRepository = required(workflowStateRepository, "workflowStateRepository");
        log.info("Registered workflows = {}", this.workflowFactories.keySet());
    }

    @Override
    public Mono<ClaimTicket> deleteWorkflowContext(final String workflowName, final String claimTicketNumber) {
        return Mono.just(claimTicketNumber)
                .map(ctn -> workflowStateRepository
                        .find(ctn, IWorkflowContext.class)
                        .orElseThrow(() -> new UnknownClaimTicketException(ctn)))
                .map(ctx -> {
                    log.info("Attempting to delete {}", ctx);
                    if (ctx.getWorkflowName().equals(workflowName)) {
                        workflowStateRepository.delete(claimTicketNumber);
                        return ClaimTicket.builder()
                                .claimTicket(ctx.getClaimTicket())
                                .status(ctx.getStatus())
                                .build();
                    } else {
                        throw new MismatchedClaimTicketException(claimTicketNumber, workflowName);
                    }
                });
    }

    @Override
    public Mono<ClaimTicket> getWorkflowStatus(final String claimTicketNumber) {
        return Mono.just(claimTicketNumber)
                .map(ctn -> workflowStateRepository
                        .find(ctn, IWorkflowContext.class)
                        .orElseThrow(() -> new UnknownClaimTicketException(ctn)))
                .map(ctx -> ClaimTicket.builder()
                        .claimTicket(ctx.getClaimTicket())
                        .status(ctx.getStatus())
                        .build());
    }

    @Override
    public <C extends IWorkflowContext> Mono<C> runWorkflow(final C initialContext, final Class<C> contextClass) {
        final IWorkflowFactory<IWorkflowContext> workflowFactory = findWorkflowFactory(initialContext);
        try {

            return workflowFactory.createWorkflow(initialContext).cast(contextClass);

        } catch(final Exception e) {
            initialContext.setStatus(WorkflowStatus.FAILED);
            throw new WorkflowException(
                    "Unexpected error occurred while running a workflow - claimTicket = " + initialContext.getClaimTicket(),
                    e,
                    initialContext
            );
        }
    }

    private String calculateWorkflowName(final String key) {
        final String base = key.replaceFirst("WorkflowFactory", "");
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, base);
    }

    @SuppressWarnings("unchecked")
    private IWorkflowFactory<IWorkflowContext> findWorkflowFactory(final IWorkflowContext context) {
        final String workflowName = findWorkflowName(context);
        return Optional.ofNullable((IWorkflowFactory<IWorkflowContext>) workflowFactories.get(workflowName)).orElseThrow(
                () -> new UnknownWorkflowException(workflowName)
        );
    }

    private String findWorkflowName(final IWorkflowContext context) {
        final String workflowName = context.getWorkflowName();
        return workflowAliases.getOrDefault(workflowName, workflowName);
    }

}
