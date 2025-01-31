package uk.gov.justice.digital.hmpps.educationandworkplanapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG
import java.security.PrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

fun aValidTokenWithAuthority(
  role: String,
  username: String = "auser_gen",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    username = username,
    roles = listOf(role),
    privateKey = privateKey,
  )

fun aValidTokenWithNoAuthorities(
  username: String = "auser_gen",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    username = username,
    roles = emptyList(),
    privateKey = privateKey,
  )

fun buildAccessToken(
  username: String = "auser_gen",
  roles: List<String> = emptyList(),
  clientId: UUID = UUID.randomUUID(),
  privateKey: PrivateKey,
): String =
  Jwts.builder()
    .subject(username)
    .claims(
      mapOf(
        "authorities" to roles,
        "user_name" to username,
        "auth_source" to "nomis",
        "user_uuid" to UUID.randomUUID(),
        "client_id" to clientId,
      ),
    )
    .issuedAt(Date.from(Instant.now()))
    .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
    .signWith(privateKey, SIG.RS256)
    .compact()
