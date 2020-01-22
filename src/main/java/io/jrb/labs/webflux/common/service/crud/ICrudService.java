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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Defines the contract for a reactive service that persists {@link Entity} beans.
 *
 * @param <E> the entity class
 */
public interface ICrudService<E extends Entity<E>> {

    /**
     * Retrieve all persisted entities as a reactive stream
     *
     * @return a stream publisher of the persisted entities
     */
    Flux<E> all();

    /**
     * Persists a new entity.
     *
     * @param entity the entity to be created
     * @return a single-value publisher containing the newly-created entity
     */
    Mono<E> create(E entity);

    /**
     * Removes a persisted entity.
     *
     * @param id the entity identifier
     * @return a single-value publisher containing the removed entity
     */
    Mono<E> delete(String id);

    /**
     * Retrieves a single persisted entity using its identifier.
     *
     * @param id the entity identifier
     * @return a single-value publisher containing the corresponding entity
     */
    Mono<E> get(String id);

    /**
     * Updates a persisted entity.
     *
     * @param id the entity identifier
     * @param entity the entity updates
     * @return a single-value publisher containing the persisted entity with its new updates applied
     */
    Mono<E> update(String id, E entity);

}
