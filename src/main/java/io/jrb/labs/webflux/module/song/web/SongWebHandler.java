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

import io.jrb.labs.webflux.module.song.model.Song;
import io.jrb.labs.webflux.module.song.service.ISongService;
import lombok.extern.slf4j.Slf4j;
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
        final Mono<Song> newSong = songData.flatMap(song -> songService.create(song));
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(newSong, Song.class));
    }

    public Mono<ServerResponse> deleteSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        final Mono<Song> deletedSong = songService.delete(songId);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(deletedSong, Song.class));
    }

    public Mono<ServerResponse> getSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        final Mono<Song> foundSong = songService.get(songId);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(foundSong, Song.class));
    }

    public Mono<ServerResponse> retrieveSongs(final ServerRequest request) {
        final Flux<Song> foundSong = songService.all();
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(foundSong, Song.class));
    }

    public Mono<ServerResponse> updateSong(final ServerRequest request) {
        final String songId = request.pathVariable("songId");
        final Mono<Song> songData = request.body(BodyExtractors.toMono(Song.class));
        final Mono<Song> updatedSong = songData.flatMap(song -> songService.update(songId, song));
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(updatedSong, Song.class));
    }

}
