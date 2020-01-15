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

import io.jrb.labs.webflux.module.security.model.User;
import io.jrb.labs.webflux.module.security.web.PBKDF2Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
public class AuthenticationService implements IAuthenticationService {

    private static final String SECURITY_ROLE_PREFIX = "ROLE_";

    private final LdapTemplate ldapTemplate;
    private final PBKDF2Encoder passwordEncoder;

    public AuthenticationService(
            final LdapTemplate ldapTemplate,
            final PBKDF2Encoder passwordEncoder
    ) {
        this.ldapTemplate = ldapTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> authenticate(final String username, final String password) {
        final AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", username));
        if (ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), passwordEncoder.encode(password))) {
            final User user = findUserByUsername(username);
            log.info("user = {}", user);
            return Mono.just(user);
        }
        return Mono.empty();
    }

    private User findUserByUsername(final String username) {
        final LdapQuery query = query()
                .where("objectclass").is("groupOfUniqueNames")
                .and("uniqueMember").is(getDnForUser(username));

        final List<GrantedAuthority> grantedAuths = ldapTemplate.search(
                query,
                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        ).stream()
                .map(group -> String.format("%s%s", SECURITY_ROLE_PREFIX, group.toUpperCase()))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new User(username, "", true, grantedAuths);
    }

    private String getDnForUser(String uid) {
        final Filter f = new EqualsFilter("uid", uid);
        final List<String> result = ldapTemplate.search(
                LdapUtils.emptyLdapName(),
                f.toString(),
                new AbstractContextMapper<String>() {
                    protected String doMapFromContext(DirContextOperations ctx) {
                        return ctx.getNameInNamespace();
                    }
                });

        if (result.size() != 1) {
            throw new RuntimeException("User not found or not unique");
        }

        return result.get(0);
    }

}
