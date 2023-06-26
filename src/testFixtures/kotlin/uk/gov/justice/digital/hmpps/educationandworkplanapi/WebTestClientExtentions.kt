package uk.gov.justice.digital.hmpps.educationandworkplanapi

import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec
import reactor.core.publisher.Mono

fun WebTestClient.RequestHeadersSpec<*>.bearerToken(bearerToken: String): WebTestClient.RequestBodySpec =
  header("authorization", "Bearer $bearerToken") as WebTestClient.RequestBodySpec

fun <T : Any> WebTestClient.RequestBodySpec.withBody(requestBody: T): WebTestClient.RequestBodySpec =
  body(
    Mono.just(requestBody),
    requestBody.javaClass,
  ) as RequestBodySpec
