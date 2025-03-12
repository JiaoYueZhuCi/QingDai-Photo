package com.qingdai.config;

import com.qingdai.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // .authorizeHttpRequests(auth -> auth
                // .requestMatchers("/photo/getVisiblePhotosByPage",
                // "/photo/getThumbnail100KPhotos",
                // "/photo/getThumbnail100KPhoto",
                // "/photo/getThumbnail1000KPhoto")
                // .permitAll()
                // .anyRequest().authenticated()
                // )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 配置会话管理策略
                .sessionManagement(session -> {
                    // 设置会话创建策略为无状态，即不使用会话来管理用户状态
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}