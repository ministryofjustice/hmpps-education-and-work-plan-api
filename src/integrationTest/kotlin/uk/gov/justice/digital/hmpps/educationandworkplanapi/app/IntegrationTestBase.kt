package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.microsoft.applicationinsights.TelemetryClient
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import java.security.KeyPair
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {

  init {
    // set awaitility defaults
    Awaitility.setDefaultPollInterval(500, MILLISECONDS)
    Awaitility.setDefaultTimeout(3, SECONDS)
  }

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var actionPlanRepository: ActionPlanRepository

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  @Autowired
  lateinit var keyPair: KeyPair

  @BeforeEach
  fun clearDatabase() {
    actionPlanRepository.deleteAll() // Will also remove all Goals and Steps due to cascade
  }
}
