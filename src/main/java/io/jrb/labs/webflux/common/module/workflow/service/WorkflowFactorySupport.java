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

import io.jrb.labs.webflux.common.module.workflow.model.WorkflowStatus;
import reactor.core.publisher.Mono;

public abstract class WorkflowFactorySupport<C extends IWorkflowContext> implements IWorkflowFactory<C> {

    private final Class<C> contextClass;
    private final IWorkflowStateRepository workflowStateRepository;

    public WorkflowFactorySupport(final Class<C> contextClass, final IWorkflowStateRepository workflowStateRepository) {
        this.contextClass = contextClass;
        this.workflowStateRepository = workflowStateRepository;
    }

    @Override
    public final Mono<C> createWorkflow(final C initialContext) {
        return Mono.just(initialContext)
                .map(ctx -> setStatus(ctx, WorkflowStatus.RUNNING))
                .flatMap(this::defineWorkflow)
                .map(ctx -> setStatus(ctx, WorkflowStatus.COMPLETED));
    }

    protected abstract Mono<C> defineWorkflow(final C context);

    protected Mono<C> runStep(final C context, final ICommand<? super C> command) {
        return command.run(context)
                .doOnSuccess(ctx -> workflowStateRepository.save(ctx.getClaimTicket(), ctx))
                .cast(contextClass);
    }

    private C setStatus(final C context, final WorkflowStatus status) {
        context.setStatus(status);
        workflowStateRepository.save(context.getClaimTicket(), context);
        return context;
    }

}

