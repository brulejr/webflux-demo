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

import io.jrb.labs.webflux.common.web.CrudWebHandlerSupport;
import io.jrb.labs.webflux.module.song.model.SetList;
import io.jrb.labs.webflux.module.song.model.SetListEntity;
import io.jrb.labs.webflux.module.song.model.SetListEntityConverter;
import io.jrb.labs.webflux.module.song.model.SetListMetadata;
import io.jrb.labs.webflux.module.song.service.ISetListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class SetListWebHandler extends CrudWebHandlerSupport<SetListEntity, SetList, SetListMetadata> {

    public SetListWebHandler(
            final ISetListService songService,
            final SetListEntityConverter setListEntityConverter
    ) {
        super(songService, setListEntityConverter, SetList.class, SetListMetadata.class, "setlistId");
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_SETLIST_CREATE')")
    public Mono<ServerResponse> createEntity(final ServerRequest request) {
        return super.createEntity(request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_SETLIST_DELETE')")
    public Mono<ServerResponse> deleteEntity(final ServerRequest request) {
        return super.deleteEntity(request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_SETLIST_READ')")
    public Mono<ServerResponse> getEntity(final ServerRequest request) {
        return super.getEntity(request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_SETLIST_READ')")
    public Mono<ServerResponse> retrieveEntities(final ServerRequest request) {
        return super.retrieveEntities(request);
    }

    @Override
    @PreAuthorize("hasAuthority('PERM_SETLIST_UPDATE')")
    public Mono<ServerResponse> updateEntity(ServerRequest request) {
        return super.updateEntity(request);
    }

}
