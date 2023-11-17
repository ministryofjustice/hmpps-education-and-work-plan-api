package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["featureToggles.ciag-induction-data-migration-enabled"], havingValue = "true")
class ApplicationStartupListener(
  private val ciagInductionMigrationService: CiagInductionMigrationService,
) : ApplicationListener<ApplicationStartedEvent> {
  override fun onApplicationEvent(event: ApplicationStartedEvent) {
    ciagInductionMigrationService.migrateCiagInductions()
  }
}
