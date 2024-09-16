package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto

@ExtendWith(MockitoExtension::class)
class ManageUserServiceTest {

  @InjectMocks
  private lateinit var manageUserService: ManageUserService

  @Mock
  private lateinit var manageUsersApiClient: ManageUsersApiClient

  @Test
  fun `should get user details`() {
    // Given
    val username = "testUser1"
    val userDetails = UserDetailsDto(username = username, active = true, name = "Test User")
    given(manageUsersApiClient.getUserDetails(username)).willReturn(userDetails)

    // When
    val actual = manageUserService.getUserDetails(username)

    // Then
    assertThat(actual).isEqualTo(userDetails)
    verify(manageUsersApiClient).getUserDetails(username)
  }

  @Test
  fun `should fail to get user details when manage-users-api is unavailable`() {
    // Given
    val username = "testUser2"
    given(manageUsersApiClient.getUserDetails(username)).willThrow(
      ManageUsersApiException("Error retrieving user details for user $username", RuntimeException()),
    )

    // When
    val exception = Assertions.assertThrows(ManageUsersApiException::class.java) {
      manageUserService.getUserDetails(username)
    }

    // Then
    verify(manageUsersApiClient).getUserDetails(username)
  }
}
