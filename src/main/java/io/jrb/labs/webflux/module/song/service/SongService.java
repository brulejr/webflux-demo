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
import io.jrb.labs.webflux.module.song.model.Song;
import io.jrb.labs.webflux.module.song.repository.ReactiveSongRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class SongService extends CrudServiceSupport<Song> implements ISongService {

    private static final Function<Song, ApplicationEvent> CREATE_EVENT_SUPPLIER = SongCreatedEvent::new;

    private static final BiFunction<Song, Song, Song> UPDATE_TRANSFORMER = (orig, update) ->
            orig.toBuilder()
                    .additionalTitles(update.getAdditionalTitles())
                    .authors(update.getAuthors())
                    .lyricOrder(update.getLyricOrder())
                    .lyrics(update.getLyrics())
                    .source(update.getSource())
                    .themes(update.getThemes())
                    .title(update.getTitle())
                    .type(update.getType())
                    .build();

    private final ReactiveSongRepository repository;

    public SongService(final ApplicationEventPublisher publisher, final ReactiveSongRepository repository) {
        super(publisher, repository, CREATE_EVENT_SUPPLIER, UPDATE_TRANSFORMER);
        this.repository = repository;
    }

    @Override
    public Mono<Song> findByTitle(final String title) {
        return repository.findFirstByTitle(title);
    }

}
