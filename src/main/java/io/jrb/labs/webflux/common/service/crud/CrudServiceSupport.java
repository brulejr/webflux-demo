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

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.jrb.labs.webflux.common.validation.Validation.required;


public abstract class CrudServiceSupport<E extends Entity<E>> implements ICrudService<E> {

    private final ApplicationEventPublisher publisher;
    private final ReactiveMongoRepository<E, String> repository;

    protected CrudServiceSupport(
            final ApplicationEventPublisher publisher,
            final ReactiveMongoRepository<E, String> repository
    ) {
        this.publisher = required(publisher, "publisher");
        this.repository = required(repository, "repository");
    }

    @Override
    public Flux<E> all() {
        return repository.findAll()
                .map(retrieveTransformer());
    }

    @Override
    public Mono<E> create(final E entity) {
        final E entityToSave = createTransformer().apply(entity);
        return repository
                .save(entityToSave)
                .doOnSuccess(e -> publisher.publishEvent(createEventSupplier().apply(e)));
    }

    @Override
    public Mono<E> delete(final String id) {
        return repository
                .findById(id)
                .flatMap(d -> repository.deleteById(id).thenReturn(d));
    }

    @Override
    public Mono<E> get(final String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<E> update(final String id, final E entity) {
        return repository
                .findById(id)
                .map(d -> updateTransformer().apply(d, entity))
                .flatMap(repository::save);
    }

    protected abstract Function<E, ApplicationEvent> createEventSupplier();

    protected Function<E, E> createTransformer() {
        return orig -> orig.withId(UUID.randomUUID().toString());
    }

    protected Function<E, E> retrieveTransformer() {
        return orig -> orig;
    }

    protected abstract BiFunction<E, E, E> updateTransformer();

}
