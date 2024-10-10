package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto

@Component
class ManageUserService(
  private val manageUsersApiClient: ManageUsersApiClient,
) {
  fun getUserDetails(username: String): UserDetailsDto =
    manageUsersApiClient.getUserDetails(username)
}
