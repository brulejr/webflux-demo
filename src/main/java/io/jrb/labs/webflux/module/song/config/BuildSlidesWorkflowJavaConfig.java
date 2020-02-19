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
package io.jrb.labs.webflux.module.song.config;

import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowStateRepository;
import io.jrb.labs.webflux.module.song.service.ISetListService;
import io.jrb.labs.webflux.module.song.service.ISongService;
import io.jrb.labs.webflux.module.song.service.workflow.buildSlides.BuildSlidesWorkflowFactory;
import io.jrb.labs.webflux.module.song.service.workflow.buildSlides.BuildSlidesWorkflowHandler;
import io.jrb.labs.webflux.module.song.service.workflow.commands.buildSlideShow.BuildSlideShowCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.buildSlideShow.BuildSlideShowConfig;
import io.jrb.labs.webflux.module.song.service.workflow.commands.buildSlideShow.IBuildSlideShowCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSetList.FindSetListCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSetList.IFindSetListCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSongsForSetList.FindSongsForSetListCommand;
import io.jrb.labs.webflux.module.song.service.workflow.commands.findSongsForSetList.IFindSongsForSetListCommand;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableConfigurationProperties(BuildSlideShowConfig.class)
public class BuildSlidesWorkflowJavaConfig {

    @Bean
    public BuildSlidesWorkflowHandler buildSlidesWorkflowHandler(final IWorkflowService workflowService) {
        return new BuildSlidesWorkflowHandler(workflowService);
    }

    @Bean
    public RouterFunction<ServerResponse> buildSlidesWorkflowEndpoints(final BuildSlidesWorkflowHandler buildSlidesWorkflowHandler) {
        return route(POST("/api/v1/workflow/build-slides/{setList}"), buildSlidesWorkflowHandler::runWorkflow);
    }

    @Bean
    public BuildSlidesWorkflowFactory buildSlidesWorkflowFactory(
            final IWorkflowStateRepository workflowStateRepository,
            final IFindSetListCommand findSetListCommand,
            final IFindSongsForSetListCommand findSongsForSetListCommand,
            final IBuildSlideShowCommand buildSlideShowCommand
    ) {
        return new BuildSlidesWorkflowFactory(workflowStateRepository, findSetListCommand, findSongsForSetListCommand, buildSlideShowCommand);
    }

    @Bean
    public IBuildSlideShowCommand buildSlideShowCommand(
            final BuildSlideShowConfig config,
            final ISongService songService
    ) {
        return new BuildSlideShowCommand(config, songService);
    }

    @Bean
    public IFindSetListCommand findSetListCommand(final ISetListService setListService) {
        return new FindSetListCommand(setListService);
    }

    @Bean
    public IFindSongsForSetListCommand findSongsForSetListCommand(final ISongService songService) {
        return new FindSongsForSetListCommand(songService);
    }

}
