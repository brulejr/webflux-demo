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
package io.jrb.labs.webflux.common.web;

import io.jrb.labs.webflux.common.service.crud.Entity;
import io.jrb.labs.webflux.common.service.crud.ICrudService;
import io.jrb.labs.webflux.common.service.crud.UnknownEntityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CrudWebHandler<E extends Entity<E>> {

    private final ICrudService<E> crudService;

    protected CrudWebHandler(ICrudService<E> crudService) {
        this.crudService = crudService;
    }

    public Mono<ServerResponse> createEntity(final ServerRequest request) {
        final Mono<E> entityData = request.body(BodyExtractors.toMono(entityClass()));
        return entityData
                .flatMap(crudService::create)
                .flatMap(this::createdEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> deleteEntity(final ServerRequest request) {
        final String songId = request.pathVariable(entityIdField());
        return Mono.just(songId)
                .flatMap(crudService::delete)
                .flatMap(this::foundEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> getEntity(final ServerRequest request) {
        final String songId = request.pathVariable(entityIdField());
        return Mono.just(songId)
                .flatMap(crudService::get)
                .flatMap(this::foundEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> retrieveEntities(final ServerRequest request) {
        final Flux<E> entities = crudService.all();
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(entities, entityClass()))
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> updateEntity(final ServerRequest request) {
        final String entityId = request.pathVariable(entityIdField());
        final Mono<E> entityData = request.body(BodyExtractors.toMono(entityClass()));
        return entityData
                .flatMap(entity -> crudService.update(entityId, entity))
                .flatMap(this::createdEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    protected HttpStatus calculateErrorStatus(final Throwable t) {
        if (t instanceof UnknownEntityException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    protected Mono<ServerResponse> createdEntityResponse(final E entity) {
        return entityResponse(entity, HttpStatus.CREATED);
    }

    protected Mono<ServerResponse> errorResponse(final Throwable t) {
        final HttpStatus status = calculateErrorStatus(t);
        final ErrorDTO errorDTO = ErrorDTO.builder()
                .errorCode(status.name())
                .description(t.getMessage())
                .build();
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorDTO));
    }

    protected Mono<ServerResponse> foundEntityResponse(final E entity) {
        return entityResponse(entity, HttpStatus.OK);
    }

    protected abstract Class<E> entityClass();

    protected abstract String entityIdField();

    private Mono<ServerResponse> entityResponse(final E entity, final HttpStatus status) {
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(entity));
    }

}
