package uk.gov.justice.digital.hmpps.educationandworkplanapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.PrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

fun aValidTokenWithViewAuthority(
  dpsUsername: String = "auser_gen",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    dpsUsername,
    listOf("ROLE_EDUCATION_WORK_PLAN_VIEWER"),
    privateKey,
  )

fun aValidTokenWithEditAuthority(
  dpsUsername: String = "auser_gen",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    dpsUsername,
    listOf("ROLE_EDUCATION_WORK_PLAN_EDITOR"),
    privateKey,
  )

fun aValidTokenWithNoAuthorities(
  dpsUsername: String = "auser_gen",
  privateKey: PrivateKey,
): String =
  buildAccessToken(
    dpsUsername,
    emptyList(),
    privateKey,
  )

fun buildAccessToken(
  dpsUsername: String = "auser_gen",
  roles: List<String> = emptyList(),
  privateKey: PrivateKey,
): String =
  Jwts.builder()
    .setSubject(dpsUsername)
    .addClaims(
      mapOf(
        "authorities" to roles,
        "user_name" to dpsUsername,
        "auth_source" to "nomis",
        "user_uuid" to UUID.randomUUID(),
      ),
    )
    .setIssuedAt(Date.from(Instant.now()))
    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
    .signWith(privateKey, SignatureAlgorithm.RS256)
    .compact()
