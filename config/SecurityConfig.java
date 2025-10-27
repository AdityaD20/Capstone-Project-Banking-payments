package com.aurionpro.app.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.aurionpro.app.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 5. DEFINE THE GLOBAL CORS CONFIGURATION BEAN
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // The origin of your Angular app
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // The HTTP methods you want to allow
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // The headers you want to allow
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // This is important for preflight requests
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all paths in your application
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 6. INTEGRATE THE CORS CONFIGURATION INTO THE SECURITY CHAIN
        http.cors(Customizer.withDefaults()); // This line tells Spring Security to use the bean we defined above

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
 	@Bean
 	AuthenticationEntryPoint authenticationEntryPoint() {
 		return (request, response, authException) -> writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
 				"UNAUTHORIZED", "Authentication is required or the token is invalid.", request);
 	}

 	@Bean
 	AccessDeniedHandler accessDeniedHandler() {
 		return (request, response, accessDeniedException) -> writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
 				"FORBIDDEN", "You do not have permission to access this resource.", request);
 	}

 	private void writeJsonError(HttpServletResponse response, int status, String code, String message,
 			HttpServletRequest request) throws IOException {
 		response.setStatus(status);
 		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
 		response.setContentType("application/json");
 		String json = """
 				{
 				  "timestamp": "%s",
 				  "status": %d,
 				  "error": "%s",
 				  "message": "%s",
 				  "path": "%s"
 				}
 				""".formatted(Instant.now().toString(), status, code, escape(message), request.getRequestURI());
 		response.getWriter().write(json);
 	}

 	private String escape(String s) {
 		if (s == null)
 			return "";
 		return s.replace("\\", "\\\\").replace("\"", "\\\"");
 	}
}