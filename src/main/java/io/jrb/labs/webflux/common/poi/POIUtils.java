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
package io.jrb.labs.webflux.common.poi;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFBackground;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.springframework.core.io.Resource;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public final class POIUtils {

    private POIUtils() {}

    /**
     * Creates a new slideshow, optionally creating leader slides based upon a list of layouts defined within the
     * master template.
     *
     * @param masterTemplate the master template
     * @param leaderSlideLayouts an optional list of leader slide layouts
     * @return the new slideshow
     * @throws IllegalArgumentException if unable to read the master template
     */
    public static XMLSlideShow createSlideshow(final Resource masterTemplate, final String... leaderSlideLayouts) {
        final XMLSlideShow xss = Optional.ofNullable(masterTemplate)
                .map(template -> {
                    try {
                        return new XMLSlideShow(template.getInputStream());
                    } catch (final IOException e) {
                        throw new IllegalArgumentException("Unable to load master template - " + template.getFilename(), e);
                    }
                })
                .orElse(new XMLSlideShow());

        createSlides(xss, leaderSlideLayouts);

        if (log.isDebugEnabled()) {
            for (final XSLFSlideMaster master : xss.getSlideMasters()) {
                for (final XSLFSlideLayout layout : master.getSlideLayouts()) {
                    log.debug("layout<{}> = {}", layout.getName(), layout.getType());
                }
            }
        }

        return xss;
    }

    /**
     * Creates a single PowerPoint slide with a black background.
     *
     * @param xss the slideshow to contains the new slide
     * @param layout the slide layout
     * @return the new slide
     */
    public static XSLFSlide createSlide(final XMLSlideShow xss, final String layout) {
        final XSLFSlide slide = xss.createSlide(xss.findLayout(layout));
        final XSLFBackground background = slide.getBackground();
        background.setFillColor(Color.BLACK);
        return slide;
    }

    /**
     * Creates one or more slides using the given layouts.
     *
     * @param xss the slideshow to contain the new slides
     * @param layouts the list of layouts
     * @return the active slideshow
     */
    public static XMLSlideShow createSlides(final XMLSlideShow xss, final String... layouts) {
        if (layouts != null) {
            for (final String layout : layouts) {
                createSlide(xss, layout);
            }
        }
        return xss;
    }

    /**
     * Creates a rectangular text paragraph on the given slide.
     *
     * @param xss the slideshow
     * @param slide the slide to modify
     * @param marginLeft the left margin
     * @param marginTop the top margin
     * @param marginRight the right margin
     * @param marginBottom the bottom margin
     * @return the paragraph
     */
    public static XSLFTextParagraph createTextRectangle(
            final XMLSlideShow xss,
            final XSLFSlide slide,
            final int marginLeft,
            final int marginTop,
            final int marginRight,
            final int marginBottom
    ) {
        final Dimension pageSize =  xss.getPageSize();
        final float x = marginLeft;
        final float y = marginTop;
        final float w = (float) pageSize.getWidth() - x - marginRight;
        final float h = (float) pageSize.getHeight() - y - marginBottom;
        final XSLFAutoShape body = slide.createAutoShape();
        body.setShapeType(ShapeType.RECT);
        body.setAnchor(new Rectangle2D.Float(x, y, w, h));
        body.clearText();
        final XSLFTextParagraph paragraph = body.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        paragraph.setBullet(false);
        return paragraph;
    }

    /**
     * Generates a byte array (raw content) of a slideshow.
     *
     * @param xss the slideshow
     * @return the corresponding raw content
     */
    public static byte[] writeSlideShowAsBytes(final XMLSlideShow xss) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            xss.write(baos);
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

}
