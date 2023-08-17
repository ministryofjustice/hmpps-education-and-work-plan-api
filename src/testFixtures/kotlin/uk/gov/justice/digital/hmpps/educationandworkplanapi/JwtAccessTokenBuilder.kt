package uk.gov.justice.digital.hmpps.educationandworkplanapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.PrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

fun aValidTokenWithViewAuthority(
  username: String = "auser_gen",
  displayName: String = "Albert User",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    username = username,
    displayName = displayName,
    roles = listOf("ROLE_EDUCATION_WORK_PLAN_VIEWER"),
    privateKey = privateKey,
  )

fun aValidTokenWithEditAuthority(
  username: String = "auser_gen",
  displayName: String = "Albert User",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    username = username,
    displayName = displayName,
    roles = listOf("ROLE_EDUCATION_WORK_PLAN_EDITOR"),
    privateKey = privateKey,
  )

fun aValidTokenWithNoAuthorities(
  username: String = "auser_gen",
  displayName: String = "Albert User",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    username = username,
    displayName = displayName,
    roles = emptyList(),
    privateKey = privateKey,
  )

fun buildAccessToken(
  username: String = "auser_gen",
  displayName: String = "Albert User",
  roles: List<String> = emptyList(),
  clientId: UUID = UUID.randomUUID(),
  privateKey: PrivateKey,
): String =
  Jwts.builder()
    .setSubject(username)
    .addClaims(
      mapOf(
        "authorities" to roles,
        "user_name" to username,
        "auth_source" to "nomis",
        "user_uuid" to UUID.randomUUID(),
        "name" to displayName,
        "client_id" to clientId,
      ),
    )
    .setIssuedAt(Date.from(Instant.now()))
    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
    .signWith(privateKey, SignatureAlgorithm.RS256)
    .compact()
