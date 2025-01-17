package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal
import java.util.Optional

class UserPrincipalAuditorAwareTest {

  companion object {
    private const val USERNAME = "auser_gen"
    private val ROLES = emptyList<GrantedAuthority>()

    private val PRINCIPAL = Principal { USERNAME }
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
  fun `should get current auditor given no authentication`() {
    // Given
    SecurityContextHolder.clearContext()

    val expected = Optional.of("system")

    // When
    val actual = auditorAware.currentAuditor

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
