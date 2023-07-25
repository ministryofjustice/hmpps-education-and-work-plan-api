package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.DpsPrincipal
import java.util.Optional

class UserPrincipalAuditorAwareTest {

  companion object {
    private const val USERNAME = "auser_gen"
    private const val DISPLAY_NAME = "Albert User"
    private val ROLES = emptyList<GrantedAuthority>()

    private val PRINCIPAL = DpsPrincipal(USERNAME, DISPLAY_NAME)
    private val AUTHENTICATION = TestingAuthenticationToken(PRINCIPAL, null, ROLES)
  }

  private val auditorAware = UserPrincipalAuditorAware()

  @AfterEach
  fun resetSpringSecurity() {
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `should get current auditor`() {
    // Given
    SecurityContextHolder.getContext().authentication = AUTHENTICATION

    val expected = Optional.of("auser_gen")

    // When
    val actual = auditorAware.currentAuditor

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should get current auditor display name`() {
    // Given
    SecurityContextHolder.getContext().authentication = AUTHENTICATION

    val expected = "Albert User"

    // When
    val actual = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should get current auditor given no authentication`() {
    // Given
    SecurityContextHolder.clearContext()

    val expected = Optional.of("system")

    // When
    val actual = auditorAware.currentAuditor

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should get current auditor display name given no authentication`() {
    // Given
    SecurityContextHolder.clearContext()

    val expected = "system"

    // When
    val actual = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
