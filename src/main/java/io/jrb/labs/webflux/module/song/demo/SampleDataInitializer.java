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
package io.jrb.labs.webflux.module.song.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jrb.labs.webflux.module.song.model.Song;
import io.jrb.labs.webflux.module.song.model.SongEntity;
import io.jrb.labs.webflux.module.song.model.SongEntityConverter;
import io.jrb.labs.webflux.module.song.repository.ReactiveSongRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class SampleDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ReactiveSongRepository songRepository;
    private final SongEntityConverter songEntityConverter;
    private final Resource songsDirectory;
    private final ObjectMapper objectMapper;

    public SampleDataInitializer(
            final ReactiveSongRepository songRepository,
            final SongEntityConverter songEntityConverter,
            final ObjectMapper objectMapper,
            final Resource songsDirectory
    ) {
        this.songRepository = songRepository;
        this.songEntityConverter = songEntityConverter;
        this.objectMapper = objectMapper;
        this.songsDirectory = songsDirectory;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        songRepository
                .deleteAll()
                .then(loadSongsFromDisk())
                .subscribe(count -> log.info("created {} songs", count));
    }

    private Mono<Long> loadSongsFromDisk() {
        try {
            return Flux.fromStream(Files.walk(Paths.get(songsDirectory.getURI())))
                    .filter(Files::isRegularFile)
                    .flatMap(this::readBytesFromPath)
                    .flatMap(this::convertBytesToSongEntity)
                    .flatMap(this::saveSongEntity)
                    .count();
        } catch (final IOException e) {
            log.error("Unable to load songs due to an error!", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Flux<byte[]> readBytesFromPath(final Path path) {
        try {
            log.info("Attempting to read song from path - {}", path);
            return Flux.just(Files.readAllBytes(path));
        } catch (final IOException e) {
            log.error("Unable to read raw bytes from {}", path);
            return Flux.empty();
        }
    }

    private Flux<SongEntity> convertBytesToSongEntity(final byte[] bytes) {
        try {
            final Song song = objectMapper.readValue(bytes, Song.class);
            final SongEntity songEntity =
                    songEntityConverter.dtoToEntity(song)
                            .withId(UUID.randomUUID().toString());
            return Flux.just(songEntity);
        } catch (final IOException e) {
            log.error("Unable to convert bytes to song");
            return Flux.empty();
        }
    }

    private Mono<SongEntity> saveSongEntity(final SongEntity songEntity) {
        return Mono.just(songEntity)
                .flatMap(songRepository::save)
                .onErrorResume(t -> {
                    log.error("Failed to save : {}", songEntity, t);
                    return Mono.empty();
                });
    }

}

