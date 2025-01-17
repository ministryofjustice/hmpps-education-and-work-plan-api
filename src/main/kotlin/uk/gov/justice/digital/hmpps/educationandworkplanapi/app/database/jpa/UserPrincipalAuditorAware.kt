package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.AuthAwareTokenConverter
import java.util.Optional

@Component
class UserPrincipalAuditorAware : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.of(
    SecurityContextHolder.getContext()?.authentication?.name ?: AuthAwareTokenConverter.SYSTEM,
  )
}
