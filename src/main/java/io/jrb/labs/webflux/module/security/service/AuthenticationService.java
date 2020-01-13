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
package io.jrb.labs.webflux.module.security.service;

import io.jrb.labs.webflux.module.security.model.Role;
import io.jrb.labs.webflux.module.security.model.User;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class AuthenticationService {

    // this is just an example, you can load the user from the database from the repository

    //username:passwowrd -> user:user
    private final String userUsername = "user";// password: user
    private final User user = new User(
            userUsername,
            "cBrlgyL2GI2GINuLUUwgojITuIufFycpLG4490dhGtY=",
            true,
            Arrays.asList(Role.ROLE_USER)
    );

    //username:passwowrd -> admin:admin
    private final String adminUsername = "admin";// password: admin
    private final User admin = new User(
            adminUsername,
            "dQNjUIMorJb8Ubj2+wVGYp6eAeYkdekqAcnYp+aRq5w=",
            true,
            Arrays.asList(Role.ROLE_ADMIN)
    );

    public Mono<User> findByUsername(String username) {
        if (username.equals(userUsername)) {
            return Mono.just(user);
        } else if (username.equals(adminUsername)) {
            return Mono.just(admin);
        } else {
            return Mono.empty();
        }
    }

}
