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
package io.jrb.labs.webflux.module.song.workflow.buildSlides;

import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
public class BuildSlidesWorkflowHandler {

    private final IWorkflowService workflowService;
    private final Scheduler streamContentScheduler;

    public BuildSlidesWorkflowHandler(
            final IWorkflowService workflowService,
            final Scheduler streamContentScheduler
    ) {
        this.workflowService = workflowService;
        this.streamContentScheduler = streamContentScheduler;
    }

    public Mono<ServerResponse> runWorkflow(final ServerRequest request) {
        final BuildSlidesWorkflowContext initialContext = BuildSlidesWorkflowContext.builder()
                .setListName(setListName(request))
                .build();
        log.info("initialContext = {}", initialContext);
        return workflowService.runWorkflow(initialContext, BuildSlidesWorkflowContext.class)
                .flatMap(ctx -> ServerResponse
                        .ok()
                        .contentType(ctx.getFinalContentType())
                        .body(BodyInserters.fromValue(ctx.getContent())));
    }

    private static String setListName(final ServerRequest r) {
        return r.pathVariable("setList");
    }

}
