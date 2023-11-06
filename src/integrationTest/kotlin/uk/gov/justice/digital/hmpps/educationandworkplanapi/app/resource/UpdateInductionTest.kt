package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import aValidCreatePrisonWorkAndEducationRequest
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkInterestsRequest
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
    createInduction(prisonNumber, aValidCreateInductionRequestWithOtherReasons())
    val persistedInduction = getInduction(prisonNumber)
    val createdDateTime = persistedInduction.createdDateTime
    val initialModifiedDateTime = persistedInduction.modifiedDateTime
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      reference = persistedInduction.reference,
      hopingToGetWork = HopingToWork.YES,
      prisonId = "MDI",
      reasonToNotGetWorkOther = null,
      abilityToWorkOther = null,
      abilityToWork = emptySet(),
      reasonToNotGetWork = emptySet(),
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
        qualifications = emptySet(),
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
      qualifications = emptySet(),
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
      .wasLastModifiedAfter(initialModifiedDateTime)
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    // check last modified dates of child objects
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(initialModifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(initialModifiedDateTime)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(initialModifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(initialModifiedDateTime)
  }

  private fun aValidCreateInductionRequestWithOtherReasons() = aValidCreateCiagInductionRequest(
    hopingToGetWork = HopingToWork.NOT_SURE,
    prisonId = "BXI",
    reasonToNotGetWorkOther = "Crime pays",
    abilityToWorkOther = "Lack of interest",
    abilityToWork = setOf(AbilityToWorkFactor.OTHER),
    reasonToNotGetWork = setOf(ReasonNotToWork.OTHER),
    workExperience = aValidCreatePreviousWorkRequest(
      typeOfWorkExperience = setOf(WorkType.OTHER),
      typeOfWorkExperienceOther = "Scientist",
      workExperience = setOf(aValidWorkExperienceResource()),
      workInterests = aValidCreateWorkInterestsRequest(),
    ),
    skillsAndInterests = aValidCreateSkillsAndInterestsRequest(
      skills = setOf(PersonalSkill.OTHER),
      skillsOther = "Hidden skills",
      personalInterests = setOf(PersonalInterest.OTHER),
      personalInterestsOther = "Secret interests",
    ),
    qualificationsAndTraining = aValidCreateEducationAndQualificationsRequest(
      educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = setOf(
        aValidAchievedQualification(),
      ),
      additionalTraining = setOf(TrainingType.OTHER),
      additionalTrainingOther = "Any training",
    ),
    inPrisonInterests = aValidCreatePrisonWorkAndEducationRequest(
      inPrisonWork = setOf(InPrisonWorkType.OTHER),
      inPrisonWorkOther = "Any in-prison work",
      inPrisonEducation = setOf(InPrisonTrainingType.OTHER),
      inPrisonEducationOther = "Any in-prison training",
    ),
  )
}
