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

import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static io.jrb.labs.webflux.common.module.workflow.web.WorkflowHandlerUtils.WORKFLOW_EXCEPTION_MAPPER;
import static io.jrb.labs.webflux.common.module.workflow.web.WorkflowHandlerUtils.claimTicketNumber;
import static io.jrb.labs.webflux.common.module.workflow.web.WorkflowHandlerUtils.workflowName;

public class CommonWorkflowHandler {

    private final IWorkflowService workflowService;

    public CommonWorkflowHandler(final IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public Mono<ServerResponse> deleteWorkflowContext(final ServerRequest request) {
        return workflowService.deleteWorkflowContext(workflowName(request), claimTicketNumber(request))
                .onErrorMap(WorkflowException.class, WORKFLOW_EXCEPTION_MAPPER)
                .flatMap(s -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(s))
                );
    }

    public Mono<ServerResponse> getWorkflowStatus(final ServerRequest request) {
        return workflowService.getWorkflowStatus(claimTicketNumber(request))
                .onErrorMap(WorkflowException.class, WORKFLOW_EXCEPTION_MAPPER)
                .flatMap(s -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(s))
                );
    }

}
