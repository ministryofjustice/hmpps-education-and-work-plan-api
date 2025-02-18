package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()

  companion object {
    const val SYSTEM_USER: String = "system"
  }

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val claims = jwt.claims

    val username = findUsername(claims)
    val authorities = extractAuthorities(jwt)

    return AuthAwareAuthenticationToken(jwt, username, authorities)
  }

  private fun findUsername(claims: Map<String, Any?>): String = if (claims.containsKey("user_name")) {
    claims["user_name"] as String
  } else {
    SYSTEM_USER
  }

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> = mutableListOf<GrantedAuthority>().apply {
    addAll(jwtGrantedAuthoritiesConverter.convert(jwt)!!)
    jwt.getClaimAsStringList("authorities")?.map(::SimpleGrantedAuthority)?.let(::addAll)
  }.toSet()
}

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val principal: String,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): Any = principal
}
