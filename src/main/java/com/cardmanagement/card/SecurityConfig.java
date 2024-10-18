package com.cardmanagement.card;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //Security configuration for the application
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //authorizeHttpRequests method is used to define the security rules for the application
        //permitAll() method is used to allow all users to access the /createuser endpoint
        //hasRole() method is used to restrict access to the /cashcards/** endpoint to users with the role CARD-OWNER
        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/createuser").permitAll()
                        .requestMatchers("/cashcards/**")
                        .hasRole("CARD-OWNER"))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    //PasswordEncoder bean is used to encode the password of the users
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    //testOnlyUsers method is used to create test users for the application

    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER")
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs456"))
                .roles("NON-OWNER")
                .build();
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xyz789"))
                .roles("CARD-OWNER")
                .build();
        return new InMemoryUserDetailsManager(sarah, hankOwnsNoCards,kumar);
    }
    //createUser method is used to create a new user with the specified username, password, and role
    public UserDetails createUser(String username, String password, String role) {
        InMemoryUserDetailsManager userDetailsService = (InMemoryUserDetailsManager) testOnlyUsers(passwordEncoder());
        UserDetails newUser = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles(role)
                .build();
        userDetailsService.createUser(newUser);
        return newUser;
    }
}
