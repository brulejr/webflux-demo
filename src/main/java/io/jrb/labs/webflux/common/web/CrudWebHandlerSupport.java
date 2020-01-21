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

public abstract class CrudWebHandlerSupport<E extends Entity<E>, D extends DTO<D>, L extends DTO<L>> {

    private final ICrudService<E> crudService;
    private final Class<D> dtoClass;
    private final Class<L> dtoLiteClass;
    private final String dtoIdField;

    protected CrudWebHandlerSupport(
            final ICrudService<E> crudService,
            final Class<D> dtoClass,
            final Class<L> dtoLiteClass,
            final String dtoIdField
    ) {
        this.crudService = crudService;
        this.dtoClass = dtoClass;
        this.dtoLiteClass = dtoLiteClass;
        this.dtoIdField = dtoIdField;
    }

    public Mono<ServerResponse> createEntity(final ServerRequest request) {
        final Mono<D> dtoData = request.body(BodyExtractors.toMono(dtoClass));
        return dtoData
                .map(this::dtoToEntity)
                .flatMap(crudService::create)
                .flatMap(this::createdResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> deleteEntity(final ServerRequest request) {
        final String dtoId = request.pathVariable(dtoIdField);
        return Mono.just(dtoId)
                .flatMap(crudService::delete)
                .flatMap(this::foundResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> getEntity(final ServerRequest request) {
        final String dtoId = request.pathVariable(dtoIdField);
        return Mono.just(dtoId)
                .flatMap(crudService::get)
                .flatMap(this::foundResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> retrieveEntities(final ServerRequest request) {
        final Flux<L> dtos = crudService.all()
                .map(this::entityToDtoLite);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(dtos, dtoLiteClass))
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> updateEntity(final ServerRequest request) {
        final String dtoId = request.pathVariable(dtoIdField);
        final Mono<D> dtoData = request.body(BodyExtractors.toMono(dtoClass));
        return dtoData
                .map(this::dtoToEntity)
                .flatMap(entity -> crudService.update(dtoId, entity))
                .flatMap(this::createdResponse)
                .onErrorResume(this::errorResponse);
    }

    protected HttpStatus calculateErrorStatus(final Throwable t) {
        if (t instanceof UnknownEntityException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    protected Mono<ServerResponse> createdResponse(final E entity) {
        return dtoResponse(entity, HttpStatus.CREATED);
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

    protected Mono<ServerResponse> foundResponse(final E entity) {
        return dtoResponse(entity, HttpStatus.OK);
    }

    protected abstract E dtoToEntity(D dto);

    protected abstract D entityToDto(E entity);

    protected abstract L entityToDtoLite(E entity);

    private Mono<ServerResponse> dtoResponse(final E entity, final HttpStatus status) {
        final D dto = entityToDto(entity);
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto));
    }

}
