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
import io.jrb.labs.webflux.module.song.model.SongDetailsDTO;
import io.jrb.labs.webflux.module.song.model.SongEntity;
import io.jrb.labs.webflux.module.song.service.ISongService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SongWebHandler extends CrudWebHandlerSupport<SongEntity, SongDetailsDTO> {

    public SongWebHandler(final ISongService songService) {
        super(songService, SongDetailsDTO.class, "songId");
    }

    @Override
    protected SongEntity dtoToEntity(final SongDetailsDTO dto) {

        final SongEntity.SongEntityBuilder builder = SongEntity.builder()
                .id(dto.getId())
                .type(dto.getType())
                .title(dto.getTitle())
                .authors(dto.getAuthors())
                .additionalTitles(dto.getAdditionalTitles())
                .themes(dto.getThemes())
                .lyrics(dto.getLyrics())
                .lyricOrder(dto.getLyricOrder())
                .source(dto.getSource());
        return builder.build();
    }

    @Override
    protected SongDetailsDTO entityToDTO(final SongEntity entity) {
        final SongDetailsDTO.SongDetailsDTOBuilder builder = SongDetailsDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .authors(entity.getAuthors())
                .additionalTitles(entity.getAdditionalTitles())
                .themes(entity.getThemes())
                .lyrics(entity.getLyrics())
                .lyricOrder(entity.getLyricOrder())
                .source(entity.getSource());
        return builder.build();
    }

}
