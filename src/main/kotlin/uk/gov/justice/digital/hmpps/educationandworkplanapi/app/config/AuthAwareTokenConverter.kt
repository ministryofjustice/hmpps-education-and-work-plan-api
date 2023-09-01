package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.HmppsAuthClient
import java.security.Principal

@Component
class AuthAwareTokenConverter(
  private val hmppsAuthClient: HmppsAuthClient,
) : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val claims = jwt.claims

    val username = findUsername(claims)
    val displayName = findDisplayName(claims) ?: username
    val activeCaseLoadId = hmppsAuthClient.getUserActiveCaseLoadId(jwt.tokenValue) ?: "unknown"
    val principal = DpsPrincipal(username, displayName, activeCaseLoadId)

    val authorities = extractAuthorities(jwt)

    return AuthAwareAuthenticationToken(jwt, principal, authorities)
  }

  private fun findUsername(claims: Map<String, Any?>): String {
    return if (claims.containsKey("user_name")) {
      claims["user_name"] as String
    } else {
      claims["client_id"] as String
    }
  }

  private fun findDisplayName(claims: Map<String, Any?>): String? {
    return claims["name"] as String?
  }

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> =
    mutableListOf<GrantedAuthority>().apply {
      addAll(jwtGrantedAuthoritiesConverter.convert(jwt)!!)
      jwt.getClaimAsStringList("authorities")?.map(::SimpleGrantedAuthority)?.let(::addAll)
    }.toSet()
}

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val principal: DpsPrincipal,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): Any {
    return principal
  }
}

class DpsPrincipal(
  private val username: String,
  val displayName: String,
  val activeCaseLoadId: String,
) : Principal {

  override fun getName(): String = username
}
