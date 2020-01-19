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
package io.jrb.labs.webflux.module.song.web;

import io.jrb.labs.webflux.common.service.crud.UnknownEntityException;
import io.jrb.labs.webflux.common.web.ErrorDTO;
import io.jrb.labs.webflux.module.song.model.Song;
import io.jrb.labs.webflux.module.song.service.ISongService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class SongWebHandler {

    private final ISongService songService;

    public SongWebHandler(final ISongService songService) {
        this.songService = songService;
    }

    public Mono<ServerResponse> createSong(final ServerRequest request) {
        final Mono<Song> songData = request.body(BodyExtractors.toMono(Song.class));
        return songData
                .flatMap(songService::create)
                .flatMap(this::createdEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> deleteSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        return Mono.just(songId)
                .flatMap(songService::delete)
                .flatMap(this::foundEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> getSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        return Mono.just(songId)
                .flatMap(songService::get)
                .flatMap(this::foundEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> retrieveSongs(final ServerRequest request) {
        final Flux<Song> foundSong = songService.all();
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(foundSong, Song.class))
                .onErrorResume(this::errorResponse);
    }

    public Mono<ServerResponse> updateSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        final Mono<Song> songData = request.body(BodyExtractors.toMono(Song.class));
        return songData
                .flatMap(song -> songService.update(songId, song))
                .flatMap(this::createdEntityResponse)
                .onErrorResume(this::errorResponse);
    }

    private HttpStatus calculateErrorStatus(final Throwable t) {
        if (t instanceof UnknownEntityException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Mono<ServerResponse> errorResponse(final Throwable t) {
        final HttpStatus status = calculateErrorStatus(t);
        final ErrorDTO errorDTO = ErrorDTO.builder()
                .errorCode(status.name())
                .description(t.getMessage())
                .build();
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorDTO));
    }

    private Mono<ServerResponse> createdEntityResponse(final Song song) {
        return entityResponse(song, HttpStatus.CREATED);
    }

    private Mono<ServerResponse> foundEntityResponse(final Song song) {
        return entityResponse(song, HttpStatus.OK);
    }

    private Mono<ServerResponse> retrievedEntityResponse(final Flux<Song> stream) {
        return entityStreamResponse(stream, HttpStatus.OK);
    }

    private Mono<ServerResponse> entityResponse(final Song song, final HttpStatus status) {
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(song));
    }

    private Mono<ServerResponse> entityStreamResponse(final Flux<Song> stream, final HttpStatus status) {
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(stream, Song.class));
    }

}
