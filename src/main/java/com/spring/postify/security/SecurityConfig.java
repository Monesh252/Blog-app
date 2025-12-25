package com.spring.postify.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource){
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT email AS username, password, true as enabled FROM users " +
                                                       "WHERE email = ?");
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery("SELECT email AS username, role AS authority FROM users " +
                                                            "WHERE email = ?");

        return jdbcUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{

        httpSecurity
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/", "/css/**",
                                                 "/posts/**","/comments/**",
                                                 "/users", "/users/**").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(
                        config -> config
                                .accessDeniedPage("/showDenied")
                )
                .formLogin(
                        form -> form
                                .loginPage("/")
                                .loginProcessingUrl("/authenticateUser")
                                .defaultSuccessUrl("/posts", true)
                                .permitAll()
                )
                .logout(logout -> logout
                        .permitAll());

        return httpSecurity.build();
    }
}
