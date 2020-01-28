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
package io.jrb.labs.webflux.module.song.service;

import io.jrb.labs.webflux.common.service.crud.CrudServiceSupport;
import io.jrb.labs.webflux.module.song.model.SetListEntity;
import io.jrb.labs.webflux.module.song.repository.ReactiveSetListRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
public class SetListService extends CrudServiceSupport<SetListEntity> implements ISetListService {

    private final ReactiveSetListRepository repository;

    public SetListService(final ApplicationEventPublisher publisher, final ReactiveSetListRepository repository) {
        super(publisher, repository, SetListEntity.class);
        this.repository = repository;
    }

    @Override
    public Mono<SetListEntity> findByName(final String name) {
        return repository.findFirstByName(name);
    }

    @Override
    protected BiFunction<SetListEntity, SetListEntity, SetListEntity> updateTransformer() {
        return (orig, update) ->
                orig.toBuilder()
                        .name(update.getName())
                        .songs(update.getSongs())
                        .build();
    }

}
