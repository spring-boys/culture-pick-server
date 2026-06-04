package com.ssafy.culturepick.global.config;

import com.ssafy.culturepick.auth.filter.JwtAuthenticationFilter;
import com.ssafy.culturepick.auth.filter.LoginFilter;
import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.oauth2.CustomOauth2MemberService;
import com.ssafy.culturepick.auth.oauth2.OAuth2FailureHandler;
import com.ssafy.culturepick.auth.oauth2.OAuth2SuccessHandler;
import com.ssafy.culturepick.auth.oauth2.Oauth2AuthorizationRequestCookieRepository;
import com.ssafy.culturepick.auth.security.CustomMemberDetailsService;
import com.ssafy.culturepick.auth.security.JwtAccessDeniedHandler;
import com.ssafy.culturepick.auth.security.JwtAuthenticationEntryPoint;
import com.ssafy.culturepick.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomMemberDetailsService memberDetailsService;

    private final CustomOauth2MemberService customOauth2MemberService;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final Oauth2AuthorizationRequestCookieRepository oauth2AuthorizationRequestCookieRepository;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/v1/dev/fetch").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cultures").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cultures/*").permitAll()
                        .anyRequest().authenticated())

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(oauth2AuthorizationRequestCookieRepository))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOauth2MemberService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))


                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(), tokenProvider, refreshTokenService, objectMapper, cookieSecure), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedMethods(List.of("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ProviderManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(memberDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(provider);
    }
}
