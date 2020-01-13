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
package io.jrb.labs.webflux.module.security;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Accessors(fluent = true) @Getter
@ConstructorBinding
@ConfigurationProperties("module.security.jwt")
public class JwtConfig {

    private final String secret;
    private final Long expirationInSec;
    private final PasswordEncoderConfig passwordEncoder;

    public JwtConfig(final String secret, final Long expirationInSec, final PasswordEncoderConfig passwordEncoder) {
        this.secret = (secret != null) ? secret : randomAlphabetic(67, 67);
        this.expirationInSec = expirationInSec;
        this.passwordEncoder = passwordEncoder;
    }

    public byte[] secretBytes() {
        return (secret != null) ? secret.getBytes() : null;
    }

}
