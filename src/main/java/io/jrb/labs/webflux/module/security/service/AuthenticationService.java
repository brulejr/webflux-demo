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

import com.google.common.collect.ImmutableList;
import io.jrb.labs.webflux.module.security.LdapConfig;
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
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.lang.String.format;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
public class AuthenticationService implements IAuthenticationService {

    private final LdapConfig ldapConfig;
    private final LdapTemplate ldapTemplate;
    private final PBKDF2Encoder passwordEncoder;

    public AuthenticationService(
            final LdapConfig ldapConfig,
            final LdapTemplate ldapTemplate,
            final PBKDF2Encoder passwordEncoder
    ) {
        this.ldapConfig = ldapConfig;
        this.ldapTemplate = ldapTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> authenticate(final String username, final String password) {
        final AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", username));
        if (ldapTemplate.authenticate(ldapConfig.userBase(), filter.toString(), passwordEncoder.encode(password))) {
            final String userDn = getDnForUser(username);
            final List<String> groupDns = findGroupDns(userDn);
            final List<String> permissionDns = findPermissionDns(userDn, groupDns);
            final List<GrantedAuthority> authorities = buildAuthoryList(groupDns, permissionDns);
            final User user = new User(username, "", true, authorities);
            log.info("user = {}", user);
            return Mono.just(user);
        }
        return Mono.empty();
    }

    private List<GrantedAuthority> buildAuthoryList(final List<String> groupDns, final List<String> permissionDns) {
        final ImmutableList.Builder<GrantedAuthority> builder = ImmutableList.builder();
        groupDns.stream()
                .map(LdapUtils::newLdapName)
                .map(name -> LdapUtils.getStringValue(name, "cn"))
                .map(group -> format("%s%s", ldapConfig.authorityPrefixRole(), group.toUpperCase()))
                .map((SimpleGrantedAuthority::new))
                .forEach(builder::add);
        permissionDns.stream()
                .map(group -> format("%s%s", ldapConfig.authorityPrefixPermission(), group.toUpperCase()))
                .map((SimpleGrantedAuthority::new))
                .forEach(builder::add);
        return builder.build();
    }

    private List<String> findGroupDns(final String userDn) {
        final List<String> groupDns = ldapTemplate.search(
                query().base(ldapConfig.groupBase())
                        .where("objectclass").is("groupOfUniqueNames")
                        .and("uniqueMember").is(userDn),
                (AttributesMapper<String>) attrs -> (String) attrs.get("entryDN").get()
        );
        log.debug("*** groupDns = {}", groupDns);
        return ImmutableList.copyOf(groupDns);
    }

    private List<String> findPermissionDns(final String userDn, final List<String> groupDns) {
        final OrFilter filter = new OrFilter();
        filter.or(new EqualsFilter("uniqueMember", userDn));
        groupDns.forEach(groupdn -> filter.or(new EqualsFilter("uniqueMember", groupdn)));
        final List<String> permDns = ldapTemplate.search(
                ldapConfig.permissionsBase(),
                filter.toString(),
                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
        log.debug("*** permDns = {}", permDns);
        return ImmutableList.copyOf(permDns);
    }

    private String getDnForUser(final String uid) {
        final Filter f = new EqualsFilter("uid", uid);
        final List<String> result = ldapTemplate.search(
                ldapConfig.userBase(),
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
