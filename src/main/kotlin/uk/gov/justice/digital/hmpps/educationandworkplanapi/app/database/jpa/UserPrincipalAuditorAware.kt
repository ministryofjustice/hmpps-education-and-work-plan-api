package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.AuthAwareTokenConverter.Companion.SYSTEM_USER
import java.util.Optional

@Component
class UserPrincipalAuditorAware : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.of(
    SecurityContextHolder.getContext()?.authentication?.principal?.let {
      it.toString()
    } ?: SYSTEM_USER,
  )
}
