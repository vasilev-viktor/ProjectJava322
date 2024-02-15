package com.example.springbasicauthexemple.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public PasswordEncoder inMemoryPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "db")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public UserDetailsService inMemoryUserDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(User
                .withUsername("user")
                .password("user")
                .roles("USER")
                .build());

        manager.createUser(User
                .withUsername("admin")
                .password("admin")
                .roles("ADMIN")
                .build());

        manager.createUser(User
                .withUsername("moderator")
                .password("12345")
                .roles("MODERATOR")
                .build());

        manager.createUser(User
                .withUsername("supervisor")
                .password("54321")
                .roles("SUPERVISOR")
                .build());

        return manager;
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "inMemory")
    public AuthenticationManager inMemoryAuthenticationManager(HttpSecurity http,
                                                               UserDetailsService inMemoryUserDetailsService) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(inMemoryUserDetailsService);
        return authManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/v1/moderator/**").hasAnyRole("MODERATOR")
                        .requestMatchers("/api/v1/supervisor/**").hasAnyRole("SUPERVISOR")
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .anyRequest().authenticated())
                        .csrf(AbstractHttpConfigurer::disable)
                        .httpBasic(Customizer.withDefaults())
                        .sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authenticationManager(authenticationManager);
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "type", havingValue = "db")
    public AuthenticationManager databaseAuthenticationManager(HttpSecurity http,
                                                               UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) throws Exception {
        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService);
        var authProvider = new DaoAuthenticationProvider(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        authManagerBuilder.authenticationProvider(authProvider);

        return authManagerBuilder.build();
    }
}
