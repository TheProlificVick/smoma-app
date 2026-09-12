package smoma.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The app has no server-rendered login form or session cookie for Spring Security itself to
 * protect — every page is static HTML calling REST endpoints with a JWT-backed identity header
 * (see JwtAuthFilter). Real authorization happens there and in AccessPolicy, not in Spring
 * Security's own role model, so this chain stays permitAll at its layer but installs
 * JwtAuthFilter ahead of it to actually verify the caller's claimed identity.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JwtAuthFilter is a @Component so it can be constructor-injected above; without this,
     * Spring Boot would ALSO auto-register it as a generic servlet filter (every Filter bean
     * gets that treatment), running it twice per request. It belongs only in the security chain.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> disableAutoRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}