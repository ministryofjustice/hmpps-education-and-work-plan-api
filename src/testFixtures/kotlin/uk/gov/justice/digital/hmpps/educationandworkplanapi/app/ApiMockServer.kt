package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import com.github.tomakehurst.wiremock.stubbing.Scenario
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import wiremock.org.eclipse.jetty.http.HttpStatus

const val WIREMOCK_PORT = 4000

class ApiMockServerExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    val apiMockServer = ApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = apiMockServer.start()
  override fun beforeEach(context: ExtensionContext): Unit = apiMockServer.resetAll()
  override fun afterAll(context: ExtensionContext): Unit = apiMockServer.stop()
}

open class ApiMockServer(
  port: Int = WIREMOCK_PORT,
) : WireMockServer(port) {
  fun stubForGet(
    path: String,
    body: String? = null,
    status: Int = HttpStatus.OK_200,
  ) = stubForGet(mappingBuilder(path = path), body, status)

  fun stubForGet(
    pathPattern: UrlPathPattern,
    body: String? = null,
    status: Int = HttpStatus.OK_200,
  ) = stubForGet(mappingBuilder(pathPattern = pathPattern), body, status)

  fun stubForRetryGet(
    scenario: String,
    path: String,
    numberOfRequests: Int = 3,
    failedStatus: Int,
    endStatus: Int,
    body: String? = null,
  ) = stubForRetryGet(scenario, mappingBuilder(path = path), numberOfRequests, failedStatus, endStatus, body)

  fun stubForRetryGet(
    scenario: String,
    pathPattern: UrlPathPattern,
    numberOfRequests: Int = 3,
    failedStatus: Int,
    endStatus: Int,
    body: String? = null,
  ) = stubForRetryGet(scenario, mappingBuilder(pathPattern = pathPattern), numberOfRequests, failedStatus, endStatus, body)

  fun stubForRetryGetWithFault(
    scenario: String,
    path: String,
    numberOfRequests: Int = 3,
    fault: Fault = Fault.CONNECTION_RESET_BY_PEER,
    endStatus: Int = 200,
    body: String? = null,
  ) = stubForRetryGetWithFault(scenario, mappingBuilder(path = path), numberOfRequests, fault, endStatus, body)

  fun stubForRetryGetWithFault(
    scenario: String,
    pathPattern: UrlPathPattern,
    numberOfRequests: Int = 3,
    fault: Fault = Fault.CONNECTION_RESET_BY_PEER,
    endStatus: Int = 200,
    body: String? = null,
  ) = stubForRetryGetWithFault(scenario, mappingBuilder(pathPattern = pathPattern), numberOfRequests, fault, endStatus, body)

  fun stubForRetryGetWithDelays(
    scenario: String,
    path: String,
    numberOfRequests: Int = 3,
    delayMs: Int = 5_000,
    endStatus: Int = 200,
    body: String? = null,
  ) = stubForRetryGetWithDelays(scenario, mappingBuilder(path = path), numberOfRequests, delayMs, endStatus, body)

  fun stubForRetryGetWithDelays(
    scenario: String,
    pathPattern: UrlPathPattern,
    numberOfRequests: Int = 3,
    delayMs: Int = 5_000,
    endStatus: Int = 200,
    body: String? = null,
  ) = stubForRetryGetWithDelays(scenario, mappingBuilder(pathPattern = pathPattern), numberOfRequests, delayMs, endStatus, body)
}

fun WireMockServer.stubForRetryGetWithFault(
  scenario: String,
  path: String,
  numberOfRequests: Int = 3,
  fault: Fault = Fault.CONNECTION_RESET_BY_PEER,
  endStatus: Int = 200,
  body: String? = null,
) = stubForRetryGetWithFault(scenario, mappingBuilder(path = path), numberOfRequests, fault, endStatus, body)

fun WireMockServer.stubForRetryGetWithDelays(
  scenario: String,
  path: String,
  numberOfRequests: Int = 3,
  delayMs: Int = 5_000,
  endStatus: Int = 200,
  body: String? = null,
) = stubForRetryGetWithDelays(scenario, mappingBuilder(path = path), numberOfRequests, delayMs, endStatus, body)

private fun WireMockServer.stubForGet(
  mappingBuilder: () -> MappingBuilder,
  body: String? = null,
  status: Int,
) {
  stubFor(
    mappingBuilder.invoke()
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status)
          .also {
            body?.let { body -> it.withBody(body.trimIndent()) }
          },
      ),
  )
}

private fun WireMockServer.stubForRetryGet(
  scenario: String,
  mappingBuilder: () -> MappingBuilder,
  numberOfRequests: Int,
  failedStatus: Int,
  endStatus: Int,
  body: String?,
) {
  (1..numberOfRequests).forEach {
    stubFor(
      mappingBuilder.invoke()
        .inScenario(scenario)
        .whenScenarioStateIs(if (it == 1) Scenario.STARTED else "RETRY${it - 1}")
        .willReturn(
          if ((failedStatus == -1 && it != numberOfRequests) || (endStatus == -1 && it == numberOfRequests)) {
            serviceUnavailable()
          } else {
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(if (it == numberOfRequests) endStatus else failedStatus)
              .also { response ->
                if (it == numberOfRequests) body?.let { body -> response.withBody(body.trimIndent()) }
              }
          },
        ).willSetStateTo("RETRY$it"),
    )
  }
}

private fun WireMockServer.stubForRetryGetWithFault(
  scenario: String,
  mappingBuilder: () -> MappingBuilder,
  numberOfRequests: Int,
  fault: Fault,
  endStatus: Int,
  body: String?,
) {
  (1..numberOfRequests).forEach {
    stubFor(
      mappingBuilder.invoke()
        .inScenario(scenario)
        .whenScenarioStateIs(if (it == 1) Scenario.STARTED else "RETRY${it - 1}")
        .willReturn(
          when (it) {
            numberOfRequests -> aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(endStatus)
              .also {
                body?.let { body -> it.withBody(body.trimIndent()) }
              }

            else -> aResponse().withFault(fault)
          },
        ).willSetStateTo("RETRY$it"),
    )
  }
}

private fun WireMockServer.stubForRetryGetWithDelays(
  scenario: String,
  mappingBuilder: () -> MappingBuilder,
  numberOfRequests: Int,
  delayMs: Int,
  endStatus: Int,
  body: String?,
) {
  (1..numberOfRequests).forEach {
    stubFor(
      mappingBuilder.invoke()
        .inScenario(scenario)
        .whenScenarioStateIs(if (it == 1) Scenario.STARTED else "RETRY${it - 1}")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(endStatus)
            .also { response ->
              body?.let { body -> response.withBody(body.trimIndent()) }
              if (it != numberOfRequests) response.withFixedDelay(delayMs)
            },
        ).willSetStateTo("RETRY$it"),
    )
  }
}

private fun WireMockServer.mappingBuilder(
  path: String? = null,
  pathPattern: UrlPathPattern? = null,
): () -> MappingBuilder = { path?.let { get(path) } ?: get(pathPattern!!) }
