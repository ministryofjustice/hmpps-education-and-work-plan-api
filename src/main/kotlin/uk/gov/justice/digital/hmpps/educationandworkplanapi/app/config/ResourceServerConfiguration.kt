package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      sessionManagement { SessionCreationPolicy.STATELESS }
      headers { frameOptions { sameOrigin = true } }
      csrf { disable() }
      authorizeHttpRequests {
        listOf(
          "/webjars/**",
          "favicon.ico",
          "/health/**",
          "/info",
          "/swagger-resources/**",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/openapi/**",
          "/queue-admin/retry-all-dlqs",
        ).forEach { authorize(it, permitAll) }
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
    }

    return http.build()
  }

  @Bean
  fun httpSessionRequestCache(): RequestCache {
    val httpSessionRequestCache = HttpSessionRequestCache()
    httpSessionRequestCache.setCreateSessionAllowed(false)
    return httpSessionRequestCache
  }
}
