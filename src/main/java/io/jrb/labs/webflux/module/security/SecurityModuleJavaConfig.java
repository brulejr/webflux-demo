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

import io.jrb.labs.webflux.common.module.ModuleJavaConfigSupport;
import io.jrb.labs.webflux.module.security.service.IAuthenticationService;
import io.jrb.labs.webflux.module.security.web.AuthenticationManager;
import io.jrb.labs.webflux.module.security.web.JwtTokenProvider;
import io.jrb.labs.webflux.module.security.web.PBKDF2Encoder;
import io.jrb.labs.webflux.module.security.web.SecurityContextRepository;
import io.jrb.labs.webflux.module.security.service.AuthenticationService;
import io.jrb.labs.webflux.module.security.web.AuthenticationController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties({ JwtConfig.class, LdapConfig.class })
public class SecurityModuleJavaConfig extends ModuleJavaConfigSupport implements WebFluxConfigurer {

    private static final String MODULE_NAME = "Security";

    public SecurityModuleJavaConfig() {
        super(MODULE_NAME, log);
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }

    @Bean
    public AuthenticationController authenticationController(
            final JwtTokenProvider jwtTokenProvider,
            final IAuthenticationService authenticationService
    ) {
        return new AuthenticationController(jwtTokenProvider, authenticationService);
    }

    @Bean
    public AuthenticationManager authenticationManager(final JwtTokenProvider jwtTokenProvider) {
        return new AuthenticationManager(jwtTokenProvider);
    }

    @Bean
    public IAuthenticationService authenticationService(
            final LdapConfig ldapConfig,
            final LdapTemplate ldapTemplate,
            final PBKDF2Encoder passwordEncoder
    ) {

        return new AuthenticationService(ldapConfig, ldapTemplate, passwordEncoder);
    }

    @Bean
    public JwtTokenProvider jwtUtil(final JwtConfig jwtConfig) {
        return new JwtTokenProvider(jwtConfig);
    }

    @Bean
    public PBKDF2Encoder pbkdf2Encoder(final JwtConfig jwtConfig) {
        return new PBKDF2Encoder(jwtConfig.passwordEncoder());
    }

    @Bean
    public SecurityContextRepository securityContextRepository(final AuthenticationManager authenticationManager) {
        return new SecurityContextRepository(authenticationManager);
    }

    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(
            final AuthenticationManager authenticationManager,
            final ServerHttpSecurity http,
            final SecurityContextRepository securityContextRepository
    ) {
        return http
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    });
                }).accessDeniedHandler((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    });
                }).and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/login").permitAll()
                .anyExchange().authenticated()
                .and().build();
    }

}
