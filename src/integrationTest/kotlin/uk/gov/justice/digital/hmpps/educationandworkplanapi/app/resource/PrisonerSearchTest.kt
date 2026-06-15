package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.FluxExchangeResult
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus.ACTIVE_PLAN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus.EXEMPT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus.NEEDS_PLAN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus.PENDING_SCREENING_AND_ASSESSMENTS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.search.assertThat
import java.time.LocalDate

class PrisonerSearchTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/search/prisons/{prisonId}/people"
    private const val PRISON_ID = "BXI"

    private val today = LocalDate.now()

    private val prisoner1 =
      aValidPrisoner(prisonerNumber = randomValidPrisonNumber(), releaseDate = today.plusYears(30))
    private val prisoner2 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
    private val prisoner3 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
    private val prisoner4 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
    private val prisoner5 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
    private val prisoner6 =
      aValidPrisoner(prisonerNumber = randomValidPrisonNumber(), firstName = "Bruce", lastName = "Wayne")
    private val prisoners = listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6)
  }

  @BeforeEach
  fun setupWiremock() {
    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      prisoners,
    )
    prisoners.forEach {
      wiremockService.stubGetPrisonerFromPrisonerSearchApi(it.prisonerNumber, it)
    }
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .bearerToken(aValidTokenWithNoAuthorities())
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/search/prisons/${PRISON_ID}/people")
  }

  @Test
  fun `should return zero results given searching for a prisoner that doesnt exist`() {
    // Given

    // When
    val response = searchPeopleWithParams("prisonerNameOrNumber" to "a name that matches nobody")

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNoPersonResponses()
      .pagination {
        it
          .isPage(1)
          .hasTotalPages(0)
          .hasTotalElements(0)
          .isFirstPage()
          .isLastPage()
      }
  }

  @Test
  fun `should return all 6 people`() {
    // Given

    // When
    val response = searchPeople()

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfPersonResponses(6)
      .pagination {
        it
          .isPage(1)
          .hasTotalPages(1)
          .hasTotalElements(6)
          .isFirstPage()
          .isLastPage()
      }
  }

  @Test
  fun `should map all display fields when prisoner-search returns only the roster fields`() {
    // RR-2692 guard: simulate prisoner-search returning ONLY the fields the roster path uses (the 5
    // fields proposed for dropping are absent). Proves the endpoint still maps correctly, so trimming
    // PrisonerSearchApiClient.RESPONSE_FIELDS is safe. Expected green BEFORE the trim.
    // Given
    val prisoner = aValidPrisoner(
      prisonerNumber = "A1234BC",
      firstName = "Alice",
      lastName = "Anderson",
      cellLocation = "A-1-001",
      dateOfBirth = LocalDate.parse("1990-01-15"),
      releaseDate = LocalDate.parse("2030-06-01"),
      receptionDate = LocalDate.parse("2020-02-02"),
    )
    wiremockService.stubPrisonersInAPrisonSearchApiReturningOnlyRosterFields(PRISON_ID, listOf(prisoner))

    // When
    val response = searchPeople()

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfPersonResponses(1)
      .personResponse(
        1,
        {
          it
            .hasPrisonNumber("A1234BC")
            .hasForename("Alice")
            .hasSurname("Anderson")
            .hasCellLocation("A-1-001")
            .hasDateOfBirth(LocalDate.parse("1990-01-15"))
            .hasReleaseDate(LocalDate.parse("2030-06-01"))
            .enteredPrisonOn(LocalDate.parse("2020-02-02"))
        },
      )
  }

  @Test
  fun `should request the expected responseFields from prisoner-search`() {
    // RR-2692 guard: pin the exact responseFields requested from prisoner-search. If RESPONSE_FIELDS
    // changes (e.g. the trim), this fails and forces a conscious update of the expected list below.
    // When
    searchPeople()

    // Then
    wiremockService.verifyPrisonersInAPrisonSearchApiCalledWithResponseFields(
      PRISON_ID,
      "prisonerNumber,legalStatus,releaseDate,receptionDate,prisonId,indeterminateSentence,recall,lastName,firstName,dateOfBirth,cellLocation,nonDtoReleaseDateType",
    )
  }

  @Nested
  inner class Sorting {
    @Test
    fun `sort by release date ascending`() {
      // Given

      // When
      val response = searchPeopleWithSort(SearchSortField.RELEASE_DATE.name)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(6)
        .personResponse(
          6,
          {
            it.hasPrisonNumber(prisoner1.prisonerNumber)
          },
        )
    }

    @Test
    fun `sort by release date descending`() {
      // Given

      // When
      val response = searchPeopleWithSort(SearchSortField.RELEASE_DATE.name, SearchSortDirection.DESC.name)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(6)
        .personResponse(
          1,
          {
            it.hasPrisonNumber(prisoner1.prisonerNumber)
          },
        )
        .personResponse(
          2,
          {
            it.hasPrisonNumber(prisoner2.prisonerNumber)
          },
        )
        .personResponse(
          3,
          {
            it.hasPrisonNumber(prisoner3.prisonerNumber)
          },
        )
        .personResponse(
          4,
          {
            it.hasPrisonNumber(prisoner4.prisonerNumber)
          },
        )
        .personResponse(
          5,
          {
            it.hasPrisonNumber(prisoner5.prisonerNumber)
          },
        )
        .personResponse(
          6,
          {
            it.hasPrisonNumber(prisoner6.prisonerNumber)
          },
        )
    }
  }

  @Nested
  inner class FilterOnNameOrPrisonNumber {
    @Test
    fun `filter on prisonerNumber`() {
      // Given

      // When
      val response = searchPeopleWithPrisonNameNumberFilter(prisoner1.prisonerNumber)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(1)
        .personResponse(
          1,
          {
            it.hasPrisonNumber(prisoner1.prisonerNumber)
          },
        )
    }

    @Test
    fun `filter on prisoner name given filter results in one result`() {
      // Given

      // When
      val response = searchPeopleWithPrisonNameNumberFilter("bruce")

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(1)
        .personResponse(
          1,
          {
            it.hasPrisonNumber(prisoner6.prisonerNumber)
          },
        )
    }

    @Test
    fun `filter on prisoner name given filter results in many results`() {
      // Given

      // When
      val response = searchPeopleWithPrisonNameNumberFilter("smith")

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(5)
        .personResponse(
          1,
          {
            it
              .hasSurname("Smith")
              .hasPrisonNumber(prisoner1.prisonerNumber)
          },
        )
        .personResponse(
          2,
          {
            it
              .hasSurname("Smith")
              .hasPrisonNumber(prisoner2.prisonerNumber)
          },
        )
        .personResponse(
          3,
          {
            it
              .hasSurname("Smith")
              .hasPrisonNumber(prisoner3.prisonerNumber)
          },
        )
        .personResponse(
          4,
          {
            it
              .hasSurname("Smith")
              .hasPrisonNumber(prisoner4.prisonerNumber)
          },
        )
        .personResponse(
          5,
          {
            it
              .hasSurname("Smith")
              .hasPrisonNumber(prisoner5.prisonerNumber)
          },
        )
    }
  }

  @Nested
  inner class FilterOnPlanStatus {

    @BeforeEach
    fun setupDatabase() {
      clearDatabase()
    }

    @Test
    fun `filter on prisoners with active plans`() {
      // Given
      // Prisoners are considered as having an active plan if they have an Induction and an Action Plan record with at least 1 goal
      // and that their Review schedule is not exempt.

      // setup prisoner 1 with a due induction and an action plan with goals - is not considered an ACTIVE_PLAN
      createInductionSchedule(prisoner1.prisonerNumber, deadlineDate = today.plusDays(1), status = SCHEDULED)
      createActionPlan(prisoner1.prisonerNumber)

      // setup prisoner 2 with an overdue induction - is not considered an ACTIVE_PLAN
      createInductionSchedule(prisoner2.prisonerNumber, deadlineDate = today.minusDays(1), status = SCHEDULED)

      // setup prisoner 6 with an exempt review - is not considered an ACTIVE_PLAN
      createReviewScheduleRecord(
        prisoner6.prisonerNumber,
        latestDate = today.plusDays(1),
        earliestDate = today.minusDays(10),
        status = ReviewScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      )

      // setup the other prisoners as having plans - they are considered ACTIVE_PLAN's
      listOf(prisoner3, prisoner4, prisoner5).forEachIndexed { idx, it ->
        createInductionSchedule(it.prisonerNumber, deadlineDate = today.plusDays(1), status = COMPLETED)
        createActionPlan(it.prisonerNumber)
        createInduction(it.prisonerNumber, aValidCreateInductionRequest())
      }

      // When
      val response = searchPeopleWithActionPlanStatus(ACTIVE_PLAN)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(3)
        .personResponse(
          1,
          {
            it
              .hasPrisonNumber(prisoner3.prisonerNumber)
              .hasPlanStatus(ACTIVE_PLAN)
          },
        )
        .personResponse(
          2,
          {
            it
              .hasPrisonNumber(prisoner4.prisonerNumber)
              .hasPlanStatus(ACTIVE_PLAN)
          },
        )
        .personResponse(
          3,
          {
            it
              .hasPrisonNumber(prisoner5.prisonerNumber)
              .hasPlanStatus(ACTIVE_PLAN)
          },
        )
    }

    @Test
    fun `filter on prisoners who need plans`() {
      // Given
      // Prisoners are considered as needing a plan if they have an Induction Schedule that is not exempt, even if they
      // have an Action Plan record with at least 1 goal (ie. they have had their goals created before their Induction)

      // setup prisoner 1 with a due induction and an action plan with goals
      createInductionSchedule(prisoner1.prisonerNumber, deadlineDate = today.plusDays(1), status = SCHEDULED)
      createActionPlan(prisoner1.prisonerNumber)

      // setup prisoner 2 with an overdue induction
      createInductionSchedule(prisoner2.prisonerNumber, deadlineDate = today.minusDays(1), status = SCHEDULED)

      // setup the other prisoners as having plans
      listOf(prisoner3, prisoner4, prisoner5, prisoner6).forEachIndexed { idx, it ->
        createInductionSchedule(it.prisonerNumber, deadlineDate = today.plusDays(1), status = COMPLETED)
        createActionPlan(it.prisonerNumber)
        createInduction(it.prisonerNumber, aValidCreateInductionRequest())
      }

      // When
      val response = searchPeopleWithActionPlanStatus(NEEDS_PLAN)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(2)
        .personResponse(
          1,
          {
            it
              .hasPrisonNumber(prisoner1.prisonerNumber)
              .hasPlanStatus(NEEDS_PLAN)
          },
        )
        .personResponse(
          2,
          {
            it
              .hasPrisonNumber(prisoner2.prisonerNumber)
              .hasPlanStatus(NEEDS_PLAN)
          },
        )
    }

    @Test
    fun `should filter by prisoners who are exempt`() {
      // Given
      // setup prisoner 3 with an exempt induction
      createInductionSchedule(
        prisoner3.prisonerNumber,
        deadlineDate = today.plusDays(1),
        status = EXEMPT_PRISONER_SAFETY_ISSUES,
      )
      // setup prisoner 6 with an exempt review
      createReviewScheduleRecord(
        prisoner6.prisonerNumber,
        latestDate = today.plusDays(1),
        earliestDate = today.minusDays(10),
        status = ReviewScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      )
      // setup the other prisoners as needing a plan
      listOf(prisoner1, prisoner2, prisoner4, prisoner5).forEach {
        createInductionSchedule(it.prisonerNumber, deadlineDate = today.plusDays(1), status = SCHEDULED)
      }

      // When
      val response = searchPeopleWithActionPlanStatus(EXEMPT)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(2)
        .personResponse(
          1,
          {
            it
              .hasPrisonNumber(prisoner3.prisonerNumber)
              .hasPlanStatus(EXEMPT)
          },
        )
        .personResponse(
          2,
          {
            it
              .hasPrisonNumber(prisoner6.prisonerNumber)
              .hasPlanStatus(EXEMPT)
          },
        )
    }

    @Test
    fun `should filter by prisoners who are pending their screenings and assessments`() {
      // Given
      // setup prisoners 3 and 6 with schedules that are pending screenings and assessments
      listOf(prisoner3, prisoner6).forEach {
        createInductionSchedule(it.prisonerNumber, status = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
      }
      // setup the other prisoners as needing a plan
      listOf(prisoner1, prisoner2, prisoner4, prisoner5).forEach {
        createInductionSchedule(it.prisonerNumber, deadlineDate = today.plusDays(1), status = SCHEDULED)
      }

      // When
      val response = searchPeopleWithActionPlanStatus(PENDING_SCREENING_AND_ASSESSMENTS)

      // Then
      val actual = response.responseBody.blockFirst()
      assertThat(actual)
        .hasNumberOfPersonResponses(2)
        .personResponse(
          1,
          {
            it
              .hasPrisonNumber(prisoner3.prisonerNumber)
              .hasPlanStatus(PENDING_SCREENING_AND_ASSESSMENTS)
          },
        )
        .personResponse(
          2,
          {
            it
              .hasPrisonNumber(prisoner6.prisonerNumber)
              .hasPlanStatus(PENDING_SCREENING_AND_ASSESSMENTS)
          },
        )
    }
  }

  private fun searchPeople(): FluxExchangeResult<PersonSearchResult> = searchPeopleWithParams()

  private fun searchPeopleWithPrisonNameNumberFilter(prisonNameNumber: String): FluxExchangeResult<PersonSearchResult> = searchPeopleWithParams("prisonerNameOrNumber" to prisonNameNumber)

  private fun searchPeopleWithActionPlanStatus(planStatus: PlanStatus): FluxExchangeResult<PersonSearchResult> = searchPeopleWithParams("planStatus" to planStatus)

  private fun searchPeopleWithSort(
    sortBy: String,
    sortDirection: String = SearchSortDirection.ASC.name,
  ): FluxExchangeResult<PersonSearchResult> = searchPeopleWithParams("sortBy" to sortBy, "sortDirection" to sortDirection)

  private fun searchPeopleWithParams(vararg params: Pair<String, Any>): FluxExchangeResult<PersonSearchResult> {
    val uri = URI_TEMPLATE + params.joinToString("&", prefix = "?") { "${it.first}=${it.second}" }

    return webTestClient.get()
      .uri(uri, PRISON_ID)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, ACTIONPLANS_RW))
      .exchange()
      .expectStatus().isOk
      .returnResult(PersonSearchResult::class.java)
  }
}
