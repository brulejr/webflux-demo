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

import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowStateRepository;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowFactorySupport;
import io.jrb.labs.webflux.module.song.service.workflow.commands.buildSlideShow.IBuildSlideShowCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSetList.IFindSetListCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSongsForSetList.IFindSongsForSetListCommand;
import reactor.core.publisher.Mono;

import static io.jrb.labs.webflux.common.validation.Validation.required;

public class BuildSlidesWorkflowFactory extends WorkflowFactorySupport<BuildSlidesWorkflowContext> {

    private final IFindSetListCommand findSetListCommand;
    private final IFindSongsForSetListCommand findSongsForSetListCommand;
    private final IBuildSlideShowCommand buildSlideShowCommand;

    public BuildSlidesWorkflowFactory(
            final IWorkflowStateRepository workflowStateRepository,
            final IFindSetListCommand findSetListCommand,
            final IFindSongsForSetListCommand findSongsForSetListCommand,
            final IBuildSlideShowCommand buildSlideShowCommand
    ) {
        super(BuildSlidesWorkflowContext.class, workflowStateRepository);
        this.findSetListCommand = required(findSetListCommand, "findSetListCommand");
        this.findSongsForSetListCommand = required(findSongsForSetListCommand, "findSongsForSetListCommand");
        this.buildSlideShowCommand = required(buildSlideShowCommand, "buildSlideShowCommand");
    }

    @Override
    protected Mono<BuildSlidesWorkflowContext> defineWorkflow(final BuildSlidesWorkflowContext context) {
        return  Mono.just(context)
                .flatMap(ctx -> runStep(ctx, findSetListCommand))
                .flatMap(ctx -> runStep(ctx, findSongsForSetListCommand))
                .flatMap(ctx -> runStep(ctx, buildSlideShowCommand));
    }

}
