package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationToCreate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationToUpdate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidCreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidUpdateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.OffsetDateTime

class UpdateEductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/person/{prisonNumber}/education"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidUpdateEducationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to update education given no education data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property reference due to missing (therefore NULL) value for creator parameter reference")
  }

  @Test
  fun `should fail to update education given education record does not exist`() {
    val prisonNumber = aValidPrisonNumber()
    val reference = aValidReference()
    val updateEducationRequest = aValidUpdateEducationRequest(
      reference = reference,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(updateEducationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
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
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Education not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should update education`() {
    val prisonNumber = aValidPrisonNumber()
    val earliestExpectedCreateTime = OffsetDateTime.now()

    createEducation(
      prisonNumber = prisonNumber,
      createEducationRequest = aValidCreateEducationRequest(
        prisonId = "BXI",
        educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
        qualifications = listOf(
          aValidAchievedQualification(
            subject = "English",
            level = QualificationLevel.LEVEL_3,
            grade = "A",
          ),
          aValidAchievedQualification(
            subject = "Maths",
            level = QualificationLevel.LEVEL_3,
            grade = "B",
          ),
          aValidAchievedQualification(
            subject = "Physics",
            level = QualificationLevel.LEVEL_4,
            grade = "C",
          ),
        ),
      ),
      username = "auser_gen",
      displayName = "Albert User",
    )

    val prisonerEducationRecord = getEducation(prisonNumber)
    val educationRecordReference = prisonerEducationRecord.reference
    val englishQualificationReference = prisonerEducationRecord.qualifications.first { it.subject == "English" }.reference
    val mathsQualificationReference = prisonerEducationRecord.qualifications.first { it.subject == "Maths" }.reference

    val updateEducationRequest = aValidUpdateEducationRequest(
      reference = educationRecordReference,
      // Updates are made in a different prison
      prisonId = "LFI",
      // Update the highest level of education
      educationLevel = EducationLevel.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(
        // Update the English qualification
        aValidAchievedQualificationToUpdate(
          reference = englishQualificationReference,
          subject = "English",
          level = QualificationLevel.LEVEL_3,
          grade = "B",
        ),
        // No changes made to the Maths qualification
        aValidAchievedQualificationToUpdate(
          reference = mathsQualificationReference,
          subject = "Maths",
          level = QualificationLevel.LEVEL_3,
          grade = "B",
        ),
        // Add a new qualification
        aValidAchievedQualificationToCreate(
          subject = "Pottery",
          level = QualificationLevel.LEVEL_1,
          grade = "Pass",
        ),
        // Physics qualification is not in the update request, so expect to be deleted
      ),
    )

    val earliestExpectedUpdateTime = OffsetDateTime.now()

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(updateEducationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
          username = "buser_gen",
          displayName = "Bernie User",
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedEducationRecord = getEducation(prisonNumber)
    assertThat(updatedEducationRecord)
      .hasEducationLevel(EducationLevel.FURTHER_EDUCATION_COLLEGE)
      .wasCreatedAtPrison("BXI")
      .wasCreatedBy("auser_gen")
      .wasCreatedByDisplayName("Albert User")
      .wasUpdatedAtPrison("LFI")
      .wasUpdatedBy("buser_gen")
      .wasUpdatedByDisplayName("Bernie User")
      .wasCreatedAtOrAfter(earliestExpectedCreateTime)
      .wasUpdatedAtOrAfter(earliestExpectedUpdateTime)
      .hasNumberOfQualifications(3)
      .qualificationBySubject("English") {
        // Expect the English qualification to have been updated
        it.hasLevel(QualificationLevel.LEVEL_3)
          .hasGrade("B")
          .wasCreatedAtPrison("BXI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedAtPrison("LFI")
          .wasUpdatedBy("buser_gen")
          .wasCreatedAtOrAfter(earliestExpectedCreateTime)
          .wasUpdatedAtOrAfter(earliestExpectedUpdateTime)
      }
      .qualificationBySubject("Maths") {
        // Expect the Maths qualification not to have been updated
        it.hasLevel(QualificationLevel.LEVEL_3)
          .hasGrade("B")
          .wasCreatedAtPrison("BXI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedAtPrison("BXI")
          .wasUpdatedBy("auser_gen")
          .wasCreatedAtOrAfter(earliestExpectedCreateTime)
          .wasUpdatedAtOrAfter(earliestExpectedCreateTime)
      }
      .qualificationBySubject("Pottery") {
        // Expect a new qualification for Pottery
        it.hasLevel(QualificationLevel.LEVEL_1)
          .hasGrade("Pass")
          .wasCreatedAtPrison("LFI")
          .wasCreatedBy("buser_gen")
          .wasUpdatedAtPrison("LFI")
          .wasUpdatedBy("buser_gen")
          .wasCreatedAtOrAfter(earliestExpectedUpdateTime)
          .wasUpdatedAtOrAfter(earliestExpectedUpdateTime)
      }
  }

  @Test
  fun `should not update education given no changes in update request`() {
    val prisonNumber = aValidPrisonNumber()

    createEducation(
      prisonNumber = prisonNumber,
      createEducationRequest = aValidCreateEducationRequest(
        prisonId = "BXI",
        educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
        qualifications = listOf(
          aValidAchievedQualification(
            subject = "English",
            level = QualificationLevel.LEVEL_3,
            grade = "A",
          ),
        ),
      ),
      username = "auser_gen",
      displayName = "Albert User",
    )

    val prisonerEducationRecord = getEducation(prisonNumber)
    val educationRecordReference = prisonerEducationRecord.reference
    val englishQualificationReference = prisonerEducationRecord.qualifications.first { it.subject == "English" }.reference

    val updateEducationRequest = aValidUpdateEducationRequest(
      reference = educationRecordReference,
      prisonId = "BXI",
      educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidAchievedQualificationToUpdate(
          reference = englishQualificationReference,
          subject = "English",
          level = QualificationLevel.LEVEL_3,
          grade = "A",
        ),
      ),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(updateEducationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedEducationRecord = getEducation(prisonNumber)
    // assert no changes on the education record after the update vs. the one before the update
    assertThat(updatedEducationRecord).isEqualTo(prisonerEducationRecord)
  }
}
