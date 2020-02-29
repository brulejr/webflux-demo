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

import io.jrb.labs.webflux.module.song.model.SetListEntityConverter;
import io.jrb.labs.webflux.module.song.repository.ReactiveSetListRepository;
import io.jrb.labs.webflux.module.song.service.ISetListService;
import io.jrb.labs.webflux.module.song.service.SetListService;
import io.jrb.labs.webflux.module.song.web.SetListWebHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SetListServiceJavaConfig {

    @Bean
    public RouterFunction<ServerResponse> setListEndpoints(
            final SongModuleConfig songModuleConfig,
            final SetListWebHandler setListWebHandler
    ) {
        final String baseResource = songModuleConfig.resources().getOrDefault("setlist", "/setlist");
        final String individualResource = baseResource + "/{setlistId}";
        return route(
                POST(baseResource)
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::createEntity
        ).andRoute(
                DELETE(individualResource)
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::deleteEntity
        ).andRoute(
                GET(individualResource



                )
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::getEntity
        ).andRoute(
                GET(baseResource)
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                setListWebHandler::retrieveEntities
        ).andRoute(
                PUT(individualResource)
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

}
