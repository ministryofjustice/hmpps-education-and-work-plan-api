package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.anotherValidAchievedQualification

class GetEducationTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/person/{prisonNumber}/education"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, randomValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return 404 if no education exists for prisoner yet`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual)
      .hasStatus(404)
      .hasUserMessage("Education not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should get education for prisoner who has had their previous qualifications setup as part of their Induction`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val previousQualifications = aValidCreatePreviousQualificationsRequest(
      educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidAchievedQualification(),
        anotherValidAchievedQualification(),
      ),
    )
    createInduction(
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(
        previousQualifications = previousQualifications,
      ),
    )

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(EducationResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual)
      .hasEducationLevel(EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS)
      .hasNumberOfQualifications(2)
      .qualificationBySubject("English") {
        it.hasLevel(QualificationLevel.LEVEL_3)
          .hasGrade("A")
      }
      .qualificationBySubject("Maths") {
        it.hasLevel(QualificationLevel.LEVEL_3)
          .hasGrade("B")
      }
  }
}
