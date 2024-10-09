package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class ManageUsersApiClient(@Qualifier("manageUsersApiWebClient") private val manageUsersApiClient: WebClient) {
  fun getUserDetails(username: String): UserDetailsDto {
    return try {
      manageUsersApiClient
        .get()
        .uri("/users/{username}", username)
        .retrieve()
        .bodyToMono(UserDetailsDto::class.java)
        .block()!!
    } catch (e: WebClientResponseException.NotFound) {
      UserDetailsDto(username, false, "$username not found")
    } catch (e: Exception) {
      throw ManageUsersApiException("Error retrieving user details for user $username", e)
    }
  }
}

data class UserDetailsDto(
  val username: String,
  val active: Boolean,
  val name: String,
)
