package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.util.retry.Retry
import java.time.Duration

@Component
class WebClientExtension(
  @param:Value("\${api.max-retry-attempts:3}") private val maxRetryAttempts: Long,
  @param:Value("\${api.min-back-off-duration:3s}") private val minBackOffDuration: Duration,
  @param:Value("\${api.status-code-retry-exhausted:503}") private val statusCodeRetryExhausted: Int,
) {
  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * This Retry can only be used with idempotent request (e.g. `GET`)
   */
  fun retryForIdempotentRequest(
    uri: String,
    upstream: String,
    statusCodeRetryExhausted: Int = this.statusCodeRetryExhausted,
  ): Retry = Retry.backoff(maxRetryAttempts, minBackOffDuration)
    .filter { it.isSafeToRetry() }
    .onRetryExhaustedThrow { _, retrySignal ->
      throw UpstreamResponseException(
        message = "Failed to process after ${retrySignal.totalRetries()} retries",
        statusCode = statusCodeRetryExhausted,
        uri = uri,
        upstream = upstream,
        cause = retrySignal.failure().cause,
      )
    }.doBeforeRetry { log.debug("WebClient Retry #{} for failure {}", it.totalRetries(), it.failure().message) }

  private fun Throwable.isSafeToRetry() = when (this) {
    is WebClientRequestException -> true
    else -> false
  }
}

data class UpstreamResponseException(
  override val message: String?,
  val statusCode: Int,
  val uri: String? = null,
  val upstream: String? = null,
  override val cause: Throwable? = null,
) : RuntimeException(message)
