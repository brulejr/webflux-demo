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
package io.jrb.labs.webflux.common.module.workflow.web;

import io.jrb.labs.webflux.common.module.workflow.service.IFinalContentWorkflowContext;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowContext;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static io.jrb.labs.webflux.common.module.workflow.web.WorkflowHandlerUtils.WORKFLOW_EXCEPTION_MAPPER;

@Slf4j
public class WorkflowHandlerSupport<C extends IWorkflowContext> {

    private final IWorkflowService workflowService;
    private final Class<C> workflowContextClass;
    private final Function<ServerRequest, C> initialContextBuilder;
    private final Function<C, Mono<ServerResponse>> responseBuilder;

    public WorkflowHandlerSupport(
            final IWorkflowService workflowService,
            final Class<C> workflowContextClass,
            final Function<ServerRequest, C> initialContextBuilder
    ) {
        this.workflowService = workflowService;
        this.workflowContextClass = workflowContextClass;
        this.initialContextBuilder = initialContextBuilder;
        this.responseBuilder = ctx -> {
            final ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
            if (ctx instanceof IFinalContentWorkflowContext) {
                final IFinalContentWorkflowContext<?> fctx = (IFinalContentWorkflowContext<?>) ctx;
                return bodyBuilder
                        .contentType(fctx.getFinalContentType())
                        .body(BodyInserters.fromValue(fctx.getFinalContent()));
            } else {
                return bodyBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ctx.getStatus());
            }
        };
    }

    public WorkflowHandlerSupport(
            final IWorkflowService workflowService,
            final Class<C> workflowContextClass,
            final Function<ServerRequest, C> initialContextBuilder,
            final Function<C, Mono<ServerResponse>> responseBuilder
    ) {
        this.workflowService = workflowService;
        this.workflowContextClass = workflowContextClass;
        this.initialContextBuilder = initialContextBuilder;
        this.responseBuilder = responseBuilder;
    }

    public Mono<ServerResponse> runWorkflow(final ServerRequest request) {
        final C initialContext = initialContextBuilder.apply(request);
        return workflowService.runWorkflow(initialContext, workflowContextClass)
                .onErrorMap(WorkflowException.class, WORKFLOW_EXCEPTION_MAPPER)
                .flatMap(responseBuilder);
    }

}
