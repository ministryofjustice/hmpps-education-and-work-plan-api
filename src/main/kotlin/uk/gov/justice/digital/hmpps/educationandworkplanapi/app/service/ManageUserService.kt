package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.AuthAwareTokenConverter.Companion.SYSTEM_USER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.CacheConfiguration.Companion.USER_DETAILS

@Component
class ManageUserService(
  private val manageUsersApiClient: ManageUsersApiClient,
) {
  @Cacheable(USER_DETAILS)
  fun getUserDetails(username: String): UserDetailsDto =
    if (username == SYSTEM_USER) {
      UserDetailsDto(username, true, username)
    } else {
      manageUsersApiClient.getUserDetails(username)
    }
}
