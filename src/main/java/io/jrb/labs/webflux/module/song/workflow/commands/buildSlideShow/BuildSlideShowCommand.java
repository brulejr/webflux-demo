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
package io.jrb.labs.webflux.module.song.workflow.commands.buildSlideShow;

import io.jrb.labs.webflux.module.song.model.SongEntity;
import io.jrb.labs.webflux.module.song.service.ISongService;
import io.jrb.labs.webflux.common.poi.POIUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static io.jrb.labs.webflux.common.poi.POIUtils.createSlide;
import static io.jrb.labs.webflux.common.poi.POIUtils.createSlides;
import static io.jrb.labs.webflux.common.poi.POIUtils.writeSlideShowAsBytes;

@Slf4j
public class BuildSlideShowCommand implements IBuildSlideShowCommand {

    private final BuildSlideShowConfig config;
    private final ISongService songService;

    public BuildSlideShowCommand(
            final BuildSlideShowConfig config,
            final ISongService songService
    ) {
        this.config = config;
        this.songService = songService;
    }

    @Override
    public Mono<IBuildSlideShowContext> run(final IBuildSlideShowContext context) {
        return Flux.fromIterable(context.getSetListEntity().getSongs())
                .flatMap(songService::findByTitle)
                .collect(
                        this::createSlideshow,
                        (xss, songEntity) -> {
                            createSlide(xss, config.blankLayout());
                            addSong(xss, songEntity);
                        }
                )
                .map(xss -> createSlides(xss, config.trailerSlides()))
                .map(xss -> context
                        .setContentType(MediaType.APPLICATION_XML)
                        .setContent(writeSlideShowAsBytes(xss))
                );
    }

    private void addSong(final XMLSlideShow xss, final SongEntity songEntity) {
        for (final String stanzaName : songEntity.getLyricOrder()) {
            log.info("Adding stanzaName '{}'", stanzaName);
            final XSLFSlide slide = createSlide(xss, config.blankLayout());
            final XSLFTextParagraph paragraph = createTextRectangle(xss, slide);
            final List<String> stanzaText = songEntity.getLyrics().get(stanzaName);
            for (final String line : stanzaText) {
                final XSLFTextRun textRun = paragraph.addNewTextRun();
                textRun.setFontSize(config.fontSize());
                textRun.setFontColor(Color.LIGHT_GRAY);
                textRun.setText(line);
                paragraph.addLineBreak();
            }
        }
    }

    private XMLSlideShow createSlideshow() {
        return POIUtils.createSlideshow(config.masterTemplate(), config.leaderSlides());
    }

    private XSLFTextParagraph createTextRectangle(final XMLSlideShow xss, final XSLFSlide slide) {
        final Dimension pageSize =  xss.getPageSize();
        final float x = config.margins().left();
        final float y = config.margins().top();
        final float w = (float) pageSize.getWidth() - x - config.margins().right();
        final float h = (float) pageSize.getHeight() - y - config.margins().bottom();
        final XSLFAutoShape body = slide.createAutoShape();
        body.setShapeType(ShapeType.RECT);
        body.setAnchor(new Rectangle2D.Float(x, y, w, h));
        body.clearText();
        final XSLFTextParagraph paragraph = body.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        paragraph.setBullet(false);
        return paragraph;
    }

}
