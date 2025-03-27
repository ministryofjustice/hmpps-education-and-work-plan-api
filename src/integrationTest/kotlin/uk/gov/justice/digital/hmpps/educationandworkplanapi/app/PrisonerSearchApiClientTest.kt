package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.PostgresContainer

private val log = KotlinLogging.logger {}

/**
 * Test class to demonstrate possible problem with prisoner-search-api when calling it with paged requests
 */
@SpringBootTest
@ActiveProfiles("demo-prisoner-search-api-paging-bug")
class PrisonerSearchApiClientTest {

  companion object {
    private const val PRISON_ID = "WDI"

    private val postgresContainer = PostgresContainer.instance

    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun configureTestContainers(registry: DynamicPropertyRegistry) {
      postgresContainer?.run {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer::getUsername)
        registry.add("spring.datasource.password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_update_password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_read_only_password", postgresContainer::getPassword)
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl)
        registry.add("spring.flyway.user", postgresContainer::getUsername)
        registry.add("spring.flyway.password", postgresContainer::getPassword)
      }
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }

  @Autowired
  private lateinit var prisonerSearchApiClient: PrisonerSearchApiClient

  @Disabled
  @RepeatedTest(100)
  fun `test paged operation`() {
    // First call prisoner-search-api to get all prisoners in BXI as a single page
    val allPrisonersInOneCall = getAllPrisonersInPrison(PRISON_ID, 9999).map { it.prisonerNumber }

    assertThat(allPrisonersInOneCall.size)
      .describedAs { "The number of elements in the returned list should equal the number of elements when turning the list into a set, proving a unique set of prisoners being returned" }
      .isEqualTo(allPrisonersInOneCall.toSet().size)

    // Now get all prisoners via a series of paged calls
    val allPrisonersViaPagedCall = getAllPrisonersInPrison(PRISON_ID, 250).map { it.prisonerNumber }

    assertThat(allPrisonersViaPagedCall.size).isEqualTo(allPrisonersInOneCall.size)

    assertThat(allPrisonersViaPagedCall.size)
      .describedAs { "The number of elements in the returned list should equal the number of elements when turning the list into a set, proving a unique set of prisoners being returned" }
      .isEqualTo(allPrisonersViaPagedCall.toSet().size)
  }

  private fun getAllPrisonersInPrison(prisonId: String, pageSize: Int): List<Prisoner> {
    var page = 0

    val prisoners = mutableListOf<Prisoner>()

    do {
      val apiResponse = prisonerSearchApiClient.getPrisonersByPrisonId(
        prisonId = prisonId,
        page = page++,
        pageSize = pageSize,
      )
      prisoners.addAll(apiResponse.content)
    } while (apiResponse.last != true)

    return prisoners.toList()
      .also {
        log.info { "Returned ${it.size} prisoners for prison $prisonId from $page calls to Prisoner Search API" }
      }
  }
}
