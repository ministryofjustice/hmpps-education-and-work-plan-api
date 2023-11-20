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

  /**
   * Upon application start-up, a one-off request is made to the [CiagInductionMigrationService] to import data from the
   * CIAG API. This is to enable us to switch from creating and maintaining Prisoner Inductions within the CIAG API to
   * this API. Once this process has successfully completed, the `ciag-induction-data-migration-enabled` feature toggle
   * will be disabled.
   */
  override fun onApplicationEvent(event: ApplicationStartedEvent) {
    ciagInductionMigrationService.migrateCiagInductions()
  }
}
