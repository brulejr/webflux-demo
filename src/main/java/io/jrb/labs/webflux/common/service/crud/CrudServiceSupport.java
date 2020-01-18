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
package io.jrb.labs.webflux.common.service.crud;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.jrb.labs.webflux.common.validation.Validation.required;


public abstract class CrudServiceSupport<T> implements ICrudService<T> {

    private final ApplicationEventPublisher publisher;
    private final ReactiveMongoRepository<T, String> repository;
    private final Function<T, ApplicationEvent> createEventSupplier;
    private final Function<T , T> createTransformer;
    private final BiFunction<T, T, T> updateTransformer;

    protected CrudServiceSupport(
            final ApplicationEventPublisher publisher,
            final ReactiveMongoRepository<T, String> repository,
            final Function<T, ApplicationEvent> createEventSupplier,
            final Function<T, T> createTransformer,
            final BiFunction<T, T, T> updateTransformer
    ) {
        this.publisher = required(publisher, "publisher");
        this.repository = required(repository, "repository");
        this.createEventSupplier = required(createEventSupplier, "createEventSupplier");
        this.createTransformer = required(createTransformer, "createTransformer");
        this.updateTransformer = required(updateTransformer, "updateTransformer");
    }

    @Override
    public Flux<T> all() {
        return repository.findAll();
    }

    @Override
    public Mono<T> create(final T entity) {
        final T entityToSave = createTransformer.apply(entity);
        return repository
                .save(entityToSave)
                .doOnSuccess(e -> publisher.publishEvent(createEventSupplier.apply(e)));
    }

    @Override
    public Mono<T> delete(final String id) {
        return repository
                .findById(id)
                .flatMap(d -> repository.deleteById(id).thenReturn(d));
    }

    @Override
    public Mono<T> get(final String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<T> update(final String id, final T entity) {
        return repository
                .findById(id)
                .map(d -> updateTransformer.apply(d, entity))
                .flatMap(repository::save);
    }

}
