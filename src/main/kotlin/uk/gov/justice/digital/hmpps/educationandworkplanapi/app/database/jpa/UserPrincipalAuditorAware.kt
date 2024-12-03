package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.DpsPrincipal
import java.util.Optional

@Component
class UserPrincipalAuditorAware : AuditorAware<String> {

  companion object {
    fun getCurrentAuditorDisplayName(): String = CurrentUser().displayName
  }

  override fun getCurrentAuditor(): Optional<String> = Optional.of(CurrentUser().username)
}

class CurrentUser {
  companion object {
    private const val SYSTEM: String = "system"
  }

  val username: String
  val displayName: String

  init {
    with(SecurityContextHolder.getContext()?.authentication) {
      if (this != null && this.isAuthenticated) {
        val principal = this.principal as DpsPrincipal
        username = principal.name
        displayName = principal.displayName
      } else {
        username = SYSTEM
        displayName = SYSTEM
      }
    }
  }
}
