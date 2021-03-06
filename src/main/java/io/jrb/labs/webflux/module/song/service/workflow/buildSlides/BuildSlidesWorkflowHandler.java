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
package io.jrb.labs.webflux.module.song.service.workflow.buildSlides;

import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.web.WorkflowHandlerSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class BuildSlidesWorkflowHandler extends WorkflowHandlerSupport<BuildSlidesWorkflowContext> {

    public BuildSlidesWorkflowHandler(final IWorkflowService workflowService) {
        super(
                workflowService,
                BuildSlidesWorkflowContext.class,
                request -> BuildSlidesWorkflowContext.builder()
                    .setListName(request.pathVariable("setList"))
                    .build()
        );
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_BUILDSLIDES_WORKFLOW')")
    public Mono<ServerResponse> runWorkflow(final ServerRequest request) {
        return super.runWorkflow(request);
    }

}
