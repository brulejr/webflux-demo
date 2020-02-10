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
package io.jrb.labs.webflux.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jrb.labs.webflux.common.logging.AnnotationDrivenEventListener;
import io.jrb.labs.webflux.common.module.workflow.EnableWorkflowEngine;
import io.jrb.labs.webflux.common.web.TraceabilityHeaderNames;
import io.jrb.labs.webflux.common.web.TraceabilityWebFilter;
import io.jrb.labs.webflux.module.greeting.GreetingModuleJavaConfig;
import io.jrb.labs.webflux.module.pdf.PdfModuleJavaConfig;
import io.jrb.labs.webflux.module.security.SecurityModuleJavaConfig;
import io.jrb.labs.webflux.module.song.SongModuleJavaConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@EnableConfigurationProperties(TraceabilityHeaderNames.class)
@Import({
        GreetingModuleJavaConfig.class,
        PdfModuleJavaConfig.class,
        SecurityModuleJavaConfig.class,
        SongModuleJavaConfig.class
})
@EnableWorkflowEngine
public class ApplicationJavaConfig {

    @Bean
    public TraceabilityWebFilter traceabilityWebFilter(final TraceabilityHeaderNames traceabilityHeaderNames) {
        return new TraceabilityWebFilter(traceabilityHeaderNames);
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        return builder;
    }

    @Bean
    public AnnotationDrivenEventListener eventLogger() {
        return new AnnotationDrivenEventListener();
    }

}
