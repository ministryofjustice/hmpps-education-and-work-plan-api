package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPersonalSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkExperiencesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.isEquivalentTo
import java.time.OffsetDateTime

class GetInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should fail to get induction given induction does not exist`() {
    // Given
    val prisonNumber = "A1234BC"

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("Induction not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should get induction for prisoner not looking to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val initialDateTime = OffsetDateTime.now()
    createInduction(
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(),
      username = "asmith_gen",
    )
    val expectedWorkOnRelease = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork()
    val expectedPreviousQualifications =
      aValidPreviousQualificationsResponse(educationLevel = EducationLevel.NOT_SURE)
    val expectedPreviousTraining = aValidPreviousTrainingResponse()
    val expectedInPrisonInterests = aValidInPrisonInterestsResponse()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .wasCreatedAtOrAfter(initialDateTime)
      .wasUpdatedAtOrAfter(initialDateTime)
      .wasCreatedBy("asmith_gen")
      .wasCreatedByDisplayName("Alex Smith")
      .wasUpdatedBy("asmith_gen")
      .wasUpdatedByDisplayName("Alex Smith")
      .wasCreatedAtPrison("BXI")
      .wasUpdatedAtPrison("BXI")
      .workOnRelease { it.isEquivalentTo(expectedWorkOnRelease) }
      .previousQualifications { it.isEquivalentTo(expectedPreviousQualifications) }
      .previousTraining { it.isEquivalentTo(expectedPreviousTraining) }
      .previousWorkExperiences { it.isNull() }
      .inPrisonInterests { it.isEquivalentTo(expectedInPrisonInterests) }
      .personalSkillsAndInterests { it.isNull() }
      .futureWorkInterests { it.isNull() }
  }

  @Test
  fun `should get induction for prisoner looking to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val initialDateTime = OffsetDateTime.now()
    createInduction(
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
      username = "asmith_gen",
    )
    val expectedWorkOnRelease = aValidWorkOnReleaseResponseForPrisonerLookingToWork()
    val expectedPreviousQualifications = aValidPreviousQualificationsResponse()
    val expectedPreviousTraining = aValidPreviousTrainingResponse()
    val expectedPreviousWorkExperiences = aValidPreviousWorkExperiencesResponse()
    val expectedSkillsAndInterests = aValidPersonalSkillsAndInterestsResponse()
    val expectedFutureWorkInterests = aValidFutureWorkInterestsResponse()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .wasCreatedAtOrAfter(initialDateTime)
      .wasUpdatedAtOrAfter(initialDateTime)
      .wasCreatedBy("asmith_gen")
      .wasCreatedByDisplayName("Alex Smith")
      .wasUpdatedBy("asmith_gen")
      .wasUpdatedByDisplayName("Alex Smith")
      .wasCreatedAtPrison("BXI")
      .wasUpdatedAtPrison("BXI")
      .workOnRelease { it.isEquivalentTo(expectedWorkOnRelease) }
      .previousQualifications { it.isEquivalentTo(expectedPreviousQualifications) }
      .previousTraining { it.isEquivalentTo(expectedPreviousTraining) }
      .previousWorkExperiences { it.isEquivalentTo(expectedPreviousWorkExperiences) }
      .inPrisonInterests { it.isNull() }
      .personalSkillsAndInterests { it.isEquivalentTo(expectedSkillsAndInterests) }
      .futureWorkInterests { it.isEquivalentTo(expectedFutureWorkInterests) }
  }
}
