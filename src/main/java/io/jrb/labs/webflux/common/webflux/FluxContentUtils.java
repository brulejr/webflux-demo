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
package io.jrb.labs.webflux.common.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

@Slf4j
public final class FluxContentUtils {

    private FluxContentUtils() {}

    public static Flux<DataBuffer> streamContent(final Consumer<OutputStream> consumer, final Scheduler scheduler) {
        return Flux.create((FluxSink<DataBuffer> emitter) -> {
            final DefaultDataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer();
            try (final OutputStream dbos = dataBuffer.asOutputStream()) {
                consumer.accept(dbos);
                dbos.flush();
                emitter.next(dataBuffer);
            } catch(final IOException e) {
                log.error("Unexpected exception while streaming content!", e);
                emitter.error(e);
            }
            emitter.complete();
        }).publishOn(scheduler);
    }
}
