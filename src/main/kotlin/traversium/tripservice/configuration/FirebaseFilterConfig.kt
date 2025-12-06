package traversium.tripservice.configuration

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import traversium.tripservice.security.FirebaseAuthenticationFilter

@Configuration
@EnableWebSecurity
class FirebaseFilterConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        firebaseAuthenticationFilter: FirebaseAuthenticationFilter
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .requestMatchers("/actuator/health/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/actuator/prometheus/**").permitAll()
                    .requestMatchers("/actuator/prometheus").permitAll()
                    .requestMatchers("/graphql").authenticated()
                    .requestMatchers("/rest/**").authenticated()

                    .anyRequest().permitAll()
            }
            .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}