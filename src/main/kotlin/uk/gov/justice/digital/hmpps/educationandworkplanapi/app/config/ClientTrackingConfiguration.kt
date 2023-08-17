package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.text.ParseException

private val log = KotlinLogging.logger {}

@Configuration
class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }
}

/**
 * An MVC [HandlerInterceptor] to add the request's authentication username and clientId to the ApplicationInsights
 * customDimensions.
 */
@Configuration
class ClientTrackingInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val token = request.getHeader(HttpHeaders.AUTHORIZATION)
    val bearer = "Bearer "
    if (token.startsWith(bearer, true)) {
      try {
        val jwtBody = getClaimsFromJWT(token)

        // Add username to customDimensions if it exists in the JWT
        jwtBody.getClaim("user_name")?.apply {
          Span.current().setAttribute("username", this.toString())
        }

        // Add clientId to customDimensions if it exists in the JWT
        jwtBody.getClaim("client_id")?.apply {
          Span.current().setAttribute("clientId", this.toString())
        }
      } catch (e: ParseException) {
        log.warn("problem decoding jwt public key for application insights", e)
      }
    }
    return true
  }

  @Throws(ParseException::class)
  private fun getClaimsFromJWT(token: String): JWTClaimsSet =
    SignedJWT.parse(token.replace("Bearer ", "")).jwtClaimsSet
}
