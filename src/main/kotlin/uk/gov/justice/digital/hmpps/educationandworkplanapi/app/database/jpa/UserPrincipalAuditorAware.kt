package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class UserPrincipalAuditorAware : AuditorAware<String> {

  private companion object {
    const val SYSTEM: String = "system"
  }

  override fun getCurrentAuditor(): Optional<String> {
    return with(SecurityContextHolder.getContext().authentication) {
      if (this != null && this.isAuthenticated) {
        Optional.of(this.name)
      } else {
        Optional.of(SYSTEM)
      }
    }
  }
}
