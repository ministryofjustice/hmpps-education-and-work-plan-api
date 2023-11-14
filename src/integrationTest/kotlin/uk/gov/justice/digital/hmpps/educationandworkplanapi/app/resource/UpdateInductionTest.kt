package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import aValidUpdatePrisonWorkAndEducationRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkExperienceResource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.isEquivalentTo
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.util.UUID

class UpdateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag-inductions/{prisonNumber}"
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
      .withBody(aValidUpdateCiagInductionRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to update induction given no induction data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
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
  fun `should fail to update induction given induction does not exist`() {
    // Given
    val prisonNumber = "A1234BC"
    val updateInductionRequest = aValidUpdateCiagInductionRequest()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
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
  fun `should update induction for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedAt = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      reference = persistedInduction.reference,
      hopingToGetWork = HopingToWork.YES,
      prisonId = "MDI",
      abilityToWork = null,
      abilityToWorkOther = null,
      reasonToNotGetWork = null,
      reasonToNotGetWorkOther = null,
      workExperience = aValidUpdatePreviousWorkRequest(
        id = persistedInduction.workExperience!!.id!!,
        typeOfWorkExperience = setOf(WorkType.CONSTRUCTION),
        typeOfWorkExperienceOther = null,
        workExperience = setOf(
          aValidWorkExperienceResource(
            typeOfWorkExperience = WorkType.CONSTRUCTION,
            otherWork = null,
            role = "Bricklayer",
            details = "General brick work",
          ),
        ),
        workInterests = aValidWorkInterests(
          id = persistedInduction.workExperience!!.workInterests!!.id,
          workInterests = setOf(WorkType.SPORTS),
          workInterestsOther = null,
          particularJobInterests = setOf(
            WorkInterestDetail(
              workInterest = WorkType.SPORTS,
              role = "Football coach",
            ),
          ),
        ),
      ),
      skillsAndInterests = aValidUpdateSkillsAndInterestsRequest(
        id = persistedInduction.skillsAndInterests!!.id!!,
        skills = setOf(PersonalSkill.COMMUNICATION),
        skillsOther = null,
        personalInterests = setOf(PersonalInterest.CRAFTS),
        personalInterestsOther = null,
      ),
      qualificationsAndTraining = aValidUpdateEducationAndQualificationsRequest(
        id = persistedInduction.qualificationsAndTraining!!.id!!,
        educationLevel = HighestEducationLevel.PRIMARY_SCHOOL,
        qualifications = null,
        additionalTraining = setOf(TrainingType.CSCS_CARD),
        additionalTrainingOther = null,
      ),
      inPrisonInterests = aValidUpdatePrisonWorkAndEducationRequest(
        id = persistedInduction.inPrisonInterests!!.id!!,
        inPrisonWork = setOf(InPrisonWorkType.PRISON_LIBRARY),
        inPrisonWorkOther = null,
        inPrisonEducation = setOf(InPrisonTrainingType.WELDING_AND_METALWORK),
        inPrisonEducationOther = null,
      ),
    )

    val expectedWorkExperience = aValidPreviousWorkResponse(
      id = persistedInduction.workExperience!!.id!!,
      typeOfWorkExperience = setOf(WorkType.CONSTRUCTION),
      typeOfWorkExperienceOther = null,
      workExperience = setOf(
        aValidWorkExperienceResource(
          typeOfWorkExperience = WorkType.CONSTRUCTION,
          otherWork = null,
          role = "Bricklayer",
          details = "General brick work",
        ),
      ),
      workInterests = aValidWorkInterests(
        id = persistedInduction.workExperience!!.workInterests!!.id,
        workInterests = setOf(WorkType.SPORTS),
        workInterestsOther = null,
        particularJobInterests = setOf(
          WorkInterestDetail(
            workInterest = WorkType.SPORTS,
            role = "Football coach",
          ),
        ),
      ),
      modifiedBy = "auser_gen",
    )
    val expectedSkillsAndInterests = aValidSkillsAndInterestsResponse(
      id = persistedInduction.skillsAndInterests!!.id!!,
      skills = setOf(PersonalSkill.COMMUNICATION),
      skillsOther = null,
      personalInterests = setOf(PersonalInterest.CRAFTS),
      personalInterestsOther = null,
      modifiedBy = "auser_gen",
    )
    val expectedQualificationsAndTraining = aValidEducationAndQualificationsResponse(
      id = persistedInduction.qualificationsAndTraining!!.id!!,
      educationLevel = HighestEducationLevel.PRIMARY_SCHOOL,
      qualifications = null,
      additionalTraining = setOf(TrainingType.CSCS_CARD),
      additionalTrainingOther = null,
      modifiedBy = "auser_gen",
    )
    val expectedInPrisonInterests = aValidPrisonWorkAndEducationResponse(
      id = persistedInduction.inPrisonInterests!!.id!!,
      inPrisonWork = setOf(InPrisonWorkType.PRISON_LIBRARY),
      inPrisonWorkOther = null,
      inPrisonEducation = setOf(InPrisonTrainingType.WELDING_AND_METALWORK),
      inPrisonEducationOther = null,
      modifiedBy = "auser_gen",
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .hasHopingToGetWork(HopingToWork.YES)
      .hasPrisonId("MDI")
      .hasNoReasonToNotGetWorkOther()
      .hasNoAbilityToWorkOther()
      .hasAbilityToWork(emptySet())
      .hasReasonToNotGetWork(emptySet())
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(createdDateTime)
      .wasLastModifiedAfter(initialModifiedAt)
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    // check last modified dates of child objects
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(initialModifiedAt)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(initialModifiedAt)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(initialModifiedAt)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(initialModifiedAt)
  }

  @Test
  fun `should update induction with no qualifications`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedAt = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      originalInduction = persistedInduction,
      qualificationsAndTraining = aValidUpdateEducationAndQualificationsRequest(
        id = persistedInduction.qualificationsAndTraining!!.id!!,
        educationLevel = null,
        qualifications = null,
        additionalTraining = setOf(TrainingType.CSCS_CARD),
        additionalTrainingOther = null,
      ),
    )

    val expectedQualificationsAndTraining = aValidEducationAndQualificationsResponse(
      id = persistedInduction.qualificationsAndTraining!!.id!!,
      educationLevel = null,
      qualifications = null,
      additionalTraining = setOf(TrainingType.CSCS_CARD),
      additionalTrainingOther = null,
      modifiedBy = "auser_gen",
    )
    // Short delay for the purpose of timestamp checking
    Thread.sleep(500)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .hasHopingToGetWork(HopingToWork.NOT_SURE)
      .hasPrisonId("BXI")
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(createdDateTime)
      .wasLastModifiedAt(initialModifiedAt) // should be unchanged
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(initialModifiedAt)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
  }

  @Test
  fun `should update induction with no previous work experience`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedAt = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      originalInduction = persistedInduction,
      workExperience = aValidUpdatePreviousWorkRequest(
        id = persistedInduction.workExperience!!.id!!,
        hasWorkedBefore = false,
        typeOfWorkExperience = null,
        typeOfWorkExperienceOther = null,
        workExperience = null,
        workInterests = aValidWorkInterests(
          id = persistedInduction.workExperience!!.workInterests!!.id,
          workInterests = setOf(WorkType.SPORTS),
          workInterestsOther = null,
          particularJobInterests = setOf(
            WorkInterestDetail(
              workInterest = WorkType.SPORTS,
              role = "Football coach",
            ),
          ),
        ),
      ),
    )

    val expectedWorkExperience = aValidPreviousWorkResponse(
      id = persistedInduction.workExperience!!.id!!,
      hasWorkedBefore = false,
      typeOfWorkExperience = null,
      typeOfWorkExperienceOther = null,
      workExperience = null,
      workInterests = aValidWorkInterests(
        id = persistedInduction.workExperience!!.workInterests!!.id,
        workInterests = setOf(WorkType.SPORTS),
        workInterestsOther = null,
        particularJobInterests = setOf(
          WorkInterestDetail(
            workInterest = WorkType.SPORTS,
            role = "Football coach",
          ),
        ),
      ),
      modifiedBy = "auser_gen",
    )
    // Short delay for the purpose of timestamp checking
    Thread.sleep(500)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .hasHopingToGetWork(HopingToWork.NOT_SURE)
      .hasPrisonId("BXI")
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(createdDateTime)
      .wasLastModifiedAt(initialModifiedAt) // should be unchanged
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(initialModifiedAt)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
  }

  @Test
  fun `should update induction with no skills and interest`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedAt = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      originalInduction = persistedInduction,
      skillsAndInterests = aValidUpdateSkillsAndInterestsRequest(
        id = persistedInduction.workExperience!!.id!!,
        skills = null,
        skillsOther = null,
        personalInterests = null,
        personalInterestsOther = null,
      ),
    )

    val expectedSkillsAndInterests = aValidSkillsAndInterestsResponse(
      id = persistedInduction.workExperience!!.id!!,
      skills = null,
      skillsOther = null,
      personalInterests = null,
      personalInterestsOther = null,
      modifiedBy = "auser_gen",
    )
    // Short delay for the purpose of timestamp checking
    Thread.sleep(500)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .hasHopingToGetWork(HopingToWork.NOT_SURE)
      .hasPrisonId("BXI")
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(createdDateTime)
      .wasLastModifiedAt(initialModifiedAt) // should be unchanged
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(initialModifiedAt)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
  }

  @Test
  fun `should update induction with no in prison interests`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedAt = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      originalInduction = persistedInduction,
      inPrisonInterests = aValidUpdatePrisonWorkAndEducationRequest(
        id = persistedInduction.workExperience!!.id!!,
        inPrisonWork = null,
        inPrisonWorkOther = null,
        inPrisonEducation = null,
        inPrisonEducationOther = null,
      ),
    )

    val expectedInPrisonInterests = aValidPrisonWorkAndEducationResponse(
      id = persistedInduction.workExperience!!.id!!,
      inPrisonWork = null,
      inPrisonWorkOther = null,
      inPrisonEducation = null,
      inPrisonEducationOther = null,
      modifiedBy = "auser_gen",
    )
    // Short delay for the purpose of timestamp checking
    Thread.sleep(500)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .hasHopingToGetWork(HopingToWork.NOT_SURE)
      .hasPrisonId("BXI")
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(createdDateTime)
      .wasLastModifiedAt(initialModifiedAt) // should be unchanged
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(initialModifiedAt)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualToIgnoringNanos(initialModifiedAt)
  }

  private fun aValidUpdateCiagInductionRequest(
    originalInduction: CiagInductionResponse,
    reference: UUID = originalInduction.reference,
    hopingToGetWork: HopingToWork = originalInduction.hopingToGetWork,
    prisonId: String = originalInduction.prisonId!!,
    abilityToWork: Set<AbilityToWorkFactor>? = originalInduction.abilityToWork,
    abilityToWorkOther: String? = originalInduction.abilityToWorkOther,
    reasonToNotGetWork: Set<ReasonNotToWork>? = originalInduction.reasonToNotGetWork,
    reasonToNotGetWorkOther: String? = originalInduction.reasonToNotGetWorkOther,
    workExperience: UpdatePreviousWorkRequest? = aValidUpdatePreviousWorkRequest(
      id = originalInduction.workExperience!!.id!!,
    ),
    skillsAndInterests: UpdateSkillsAndInterestsRequest? = aValidUpdateSkillsAndInterestsRequest(
      id = originalInduction.workExperience!!.id!!,
    ),
    qualificationsAndTraining: UpdateEducationAndQualificationsRequest? = aValidUpdateEducationAndQualificationsRequest(
      id = originalInduction.workExperience!!.id!!,
    ),
    inPrisonInterests: UpdatePrisonWorkAndEducationRequest? = aValidUpdatePrisonWorkAndEducationRequest(
      id = originalInduction.workExperience!!.id!!,
    ),
  ): UpdateCiagInductionRequest =
    aValidUpdateCiagInductionRequest(
      reference = reference,
      hopingToGetWork = hopingToGetWork,
      prisonId = prisonId,
      abilityToWork = abilityToWork,
      abilityToWorkOther = abilityToWorkOther,
      reasonToNotGetWork = reasonToNotGetWork,
      reasonToNotGetWorkOther = reasonToNotGetWorkOther,
      workExperience = workExperience,
      skillsAndInterests = skillsAndInterests,
      qualificationsAndTraining = qualificationsAndTraining,
      inPrisonInterests = inPrisonInterests,
    )
}
