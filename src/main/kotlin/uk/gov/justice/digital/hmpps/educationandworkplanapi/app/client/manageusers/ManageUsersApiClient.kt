package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import java.time.Duration

private val log = KotlinLogging.logger {}

@Component
class ManageUsersApiClient(@param:Qualifier("manageUsersApiWebClient") private val manageUsersApiClient: WebClient) {
  fun getUserDetails(username: String): UserDetailsDto = try {
    manageUsersApiClient
      .get()
      .uri("/users/{username}", username)
      .retrieve()
      .bodyToMono(UserDetailsDto::class.java)
      .retryWhen(
        Retry.backoff(3, Duration.ofMillis(500))
          .filter { ex -> ex is WebClientRequestException }
          .doBeforeRetry { retrySignal ->
            log.warn(
              "Retrying request for user {} due to {} (attempt #{})",
              username,
              retrySignal.failure().javaClass.simpleName,
              retrySignal.totalRetries() + 1,
            )
          },
      )
      .block()!!
  } catch (e: WebClientResponseException.NotFound) {
    UserDetailsDto(username, false, "$username not found")
  } catch (e: Exception) {
    throw ManageUsersApiException("Error retrieving user details for user $username", e)
  }
}

data class UserDetailsDto(
  val username: String,
  val active: Boolean,
  val name: String,
)
