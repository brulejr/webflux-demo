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
package io.jrb.labs.webflux.module.song;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jrb.labs.webflux.common.module.ModuleJavaConfigSupport;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowStateRepository;
import io.jrb.labs.webflux.module.song.demo.SampleDataInitializer;
import io.jrb.labs.webflux.module.song.model.SetListEntityConverter;
import io.jrb.labs.webflux.module.song.model.SongEntityConverter;
import io.jrb.labs.webflux.module.song.repository.ReactiveSetListRepository;
import io.jrb.labs.webflux.module.song.repository.ReactiveSongRepository;
import io.jrb.labs.webflux.module.song.service.ISetListService;
import io.jrb.labs.webflux.module.song.service.ISongService;
import io.jrb.labs.webflux.module.song.service.SetListService;
import io.jrb.labs.webflux.module.song.service.SongService;
import io.jrb.labs.webflux.module.song.web.SetListWebHandler;
import io.jrb.labs.webflux.module.song.web.SongWebHandler;
import io.jrb.labs.webflux.module.song.workflow.buildSlides.BuildSlidesWorkflowFactory;
import io.jrb.labs.webflux.module.song.workflow.buildSlides.BuildSlidesWorkflowHandler;
import io.jrb.labs.webflux.module.song.workflow.commands.buildSlideShow.BuildSlideShowCommand;
import io.jrb.labs.webflux.module.song.workflow.commands.buildSlideShow.BuildSlideShowConfig;
import io.jrb.labs.webflux.module.song.workflow.commands.buildSlideShow.IBuildSlideShowCommand;
import io.jrb.labs.webflux.module.song.workflow.commands.findSetList.FindSetListCommand;
import io.jrb.labs.webflux.module.song.workflow.commands.findSetList.IFindSetListCommand;
import io.jrb.labs.webflux.module.song.workflow.commands.findSongsForSetList.FindSongsForSetListCommand;
import io.jrb.labs.webflux.module.song.workflow.commands.findSongsForSetList.IFindSongsForSetListCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "module.song.enabled")
@ComponentScan(basePackages = "io.jrb.labs.webflux.module.song.repository")
@EnableConfigurationProperties(BuildSlideShowConfig.class)
public class SongModuleJavaConfig extends ModuleJavaConfigSupport {

    private static final String MODULE_NAME = "Song";

    public SongModuleJavaConfig() {
        super(MODULE_NAME, log);
    }

    @Bean
    public BuildSlidesWorkflowHandler buildSlidesWorkflowHandler(final IWorkflowService workflowService) {
        return new BuildSlidesWorkflowHandler(workflowService, Schedulers.elastic());
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

    @Bean
    @Profile("demo")
    public SampleDataInitializer sampleDataInitializer(
            final ReactiveSongRepository songRepository,
            final SongEntityConverter songEntityConverter,
            final ObjectMapper objectMapper,
            @Value("${module.song.demo.directories.songs}")final Resource songsDirectory
    ) {
        return new SampleDataInitializer(songRepository, songEntityConverter, objectMapper, songsDirectory);
    }

    @Bean
    public RouterFunction<ServerResponse> songEndpoints(final SongWebHandler songWebHandler) {
        return route(
                POST("/song")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                songWebHandler::createEntity
        ).andRoute(
                DELETE("/song/{songId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                songWebHandler::deleteEntity
        ).andRoute(
                GET("/song/{songId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                songWebHandler::getEntity
        ).andRoute(
                GET("/song")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                songWebHandler::retrieveEntities
        ).andRoute(
                PUT("/song/{songId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                songWebHandler::updateEntity
        );
    }

    @Bean
    public RouterFunction<ServerResponse> setListEndpoints(final SetListWebHandler setListWebHandler) {
        return route(
                POST("/setlist")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::createEntity
        ).andRoute(
                RequestPredicates
                        .DELETE("/setlist/{setlistId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::deleteEntity
        ).andRoute(
                RequestPredicates
                        .GET("/setlist/{setlistId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::getEntity
        ).andRoute(
                RequestPredicates
                        .GET("/setlist")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::retrieveEntities
        ).andRoute(
                RequestPredicates
                        .PUT("/setlist/{setlistId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::updateEntity
        );
    }

    @Bean
    public SetListEntityConverter setListEntityConverter() { return new SetListEntityConverter(); }

    @Bean
    public SetListWebHandler setListWebHandler(
            final ISetListService setListService,
            final SetListEntityConverter setListEntityConverter
    ) {
        return new SetListWebHandler(setListService, setListEntityConverter);
    }

    @Bean
    public ISetListService setListService(
            final ApplicationEventPublisher publisher, final ReactiveSetListRepository setListRepository) {
        return new SetListService(publisher, setListRepository);
    }

    @Bean
    public SongEntityConverter songEntityConverter() { return new SongEntityConverter(); }

    @Bean
    public SongWebHandler songWebHandler(
            final ISongService songService,
            final SongEntityConverter songEntityConverter
    ) {
        return new SongWebHandler(songService, songEntityConverter);
    }

    @Bean
    public ISongService songService(
            final ApplicationEventPublisher publisher, final ReactiveSongRepository songRepository) {
        return new SongService(publisher, songRepository);
    }

}
