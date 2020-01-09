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
package io.jrb.labs.webflux.web;

import io.jrb.labs.webflux.common.webflux.RouterConfiguration;
import io.jrb.labs.webflux.service.pdf.IPdfService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.scheduler.Schedulers;

@RouterConfiguration
public class PdfServiceRouter {

    @Bean
    public RouterFunction<ServerResponse> createPdfDocumentEndpoint(final PdfServiceHandler pdfServiceHandler) {
        return RouterFunctions.route(
                RequestPredicates
                        .POST("/pdf/{documentId}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_PDF)),
                pdfServiceHandler::createDocument
        );
    }

    @Bean
    public PdfServiceHandler pdfServiceHandler(final IPdfService pdfService) {
        return new PdfServiceHandler(pdfService, Schedulers.elastic());
    }

}
