package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient

@Component
class ManageUserService(
  private val manageUsersApiClient: ManageUsersApiClient,
) {
  fun getUserDetails(username: String) =
    manageUsersApiClient.getUserDetails(username)
}
