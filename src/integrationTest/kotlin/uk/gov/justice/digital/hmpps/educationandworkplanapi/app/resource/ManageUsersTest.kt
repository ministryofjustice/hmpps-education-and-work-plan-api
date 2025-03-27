package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto

class ManageUsersTest : IntegrationTestBase() {
  @Test
  fun `should retry on WebClientRequestException and eventually succeed`() {
    val username = "TEST_USER"
    wiremockService.setUpManageUsersRepeatPass(username)

    val webClient = WebClient.builder()
      .baseUrl(wiremockService.getBaseUrl())
      .build()

    val apiClient = ManageUsersApiClient(webClient)
    val result = apiClient.getUserDetails(username)

    assertThat(result).isEqualTo(UserDetailsDto(username, true, "Test User"))
  }

  @Test
  fun `should retry on WebClientRequestException and fail`() {
    val username = "TEST_USER_FAIL"
    wiremockService.setUpManageUsersRepeatFail(username)

    val webClient = WebClient.builder()
      .baseUrl(wiremockService.getBaseUrl())
      .build()

    val apiClient = ManageUsersApiClient(webClient)

    val exception = assertThrows<ManageUsersApiException> {
      apiClient.getUserDetails(username)
    }
    assertThat(exception.message).contains("Error retrieving user details for user $username")
  }
}
