package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var actionPlanRepository: ActionPlanRepository

  @BeforeEach
  fun clearDatabase() {
    actionPlanRepository.deleteAll() // Will also remove all Goals and Steps due to cascade
  }
}
