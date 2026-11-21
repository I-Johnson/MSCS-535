package com.example.securedb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security configuration.
 *
 * - HTTPS only: the server opens a TLS connector on 8443 and no plain
 *   HTTP connector at all, so there is nothing unencrypted to connect to.
 *   (requiresChannel is gone in Spring Security 7; the connector setup in
 *   application.properties is what enforces HTTPS.)
 * - CSRF protection stays ENABLED. HTTP Basic credentials are cached by
 *   the browser and attached automatically to later requests to this
 *   origin, so they are ambient authority just like a session cookie -
 *   having no session does not make the API immune to CSRF. The token is
 *   delivered in an XSRF-TOKEN cookie and must be echoed back in the
 *   X-XSRF-TOKEN header on every mutating request. A cross-origin attack
 *   page can make the browser send the cached Basic credentials and the
 *   cookie, but it cannot read the token value or set the custom header
 *   on a simple cross-site request, so its mutations are rejected.
 * - The API is stateless (SessionCreationPolicy.STATELESS): no server
 *   session is created. The CSRF token lives in the cookie-based
 *   repository, not in a session.
 * - Role-based authorization: reads need USER or ADMIN, writes need ADMIN.
 * - The user store hashes passwords with BCrypt. (The synthetic demo
 *   defaults in application.properties are classroom placeholders to
 *   override with environment variables, not real secrets.)
 *
 * Note on phishing: these controls encrypt traffic, hash stored passwords,
 * require authentication and block cross-site request forgery, but they
 * cannot stop an attacker from replaying credentials an employee was
 * tricked into revealing. That is why MFA is needed in addition to a
 * password.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Plain handler: the cookie value is the token itself, so a
                // client reads the XSRF-TOKEN cookie (or GET /api/csrf) and
                // echoes it in the X-XSRF-TOKEN header. The default Xor
                // handler masks tokens against BREACH-style attacks; that is
                // not needed here because responses are not compressed.
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/csrf").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.admin-username}") String adminUsername,
            @Value("${app.security.admin-password}") String adminPassword,
            @Value("${app.security.user-username}") String userUsername,
            @Value("${app.security.user-password}") String userPassword) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build());
        manager.createUser(User.withUsername(userUsername)
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: the user store holds password hashes, not passwords.
        return new BCryptPasswordEncoder();
    }
}
