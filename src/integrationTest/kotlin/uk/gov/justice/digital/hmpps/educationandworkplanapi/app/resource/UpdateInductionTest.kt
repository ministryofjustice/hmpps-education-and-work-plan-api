package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import aValidUpdatePrisonWorkAndEducationRequest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkExperienceResource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.isEquivalentTo
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.util.UUID

class UpdateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag/induction/{prisonNumber}"
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
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
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
    val updateInductionRequest = aValidUpdateCiagInductionRequest(
      reference = null,
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
        workInterests = aValidUpdateWorkInterestsRequest(
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
      workInterests = aValidWorkInterestsResponse(
        id = persistedInduction.workExperience!!.workInterests!!.id,
        workInterests = setOf(WorkType.SPORTS),
        workInterestsOther = null,
        particularJobInterests = setOf(
          WorkInterestDetail(
            workInterest = WorkType.SPORTS,
            role = "Football coach",
          ),
        ),
        modifiedBy = "auser_gen",
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
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAfter(persistedInduction.modifiedDateTime)
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    // check last modified dates of child objects
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isAfter(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(persistedInduction.inPrisonInterests!!.modifiedDateTime)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_UPDATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", "MDI")
        .containsEntry("userId", "auser_gen")
        .containsKey("reference")
    }
  }

  @Test
  fun `should update prisoner's work aspirations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(
      prisonNumber,
      aValidCreateCiagInductionRequest(
        hopingToGetWork = HopingToWork.NOT_SURE,
        abilityToWork = setOf(AbilityToWorkFactor.OTHER),
        abilityToWorkOther = "Lack of interest",
        reasonToNotGetWork = setOf(ReasonNotToWork.OTHER),
        reasonToNotGetWorkOther = "Crime pays",
      ),
    )
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      hopingToGetWork = HopingToWork.YES,
      abilityToWork = null,
      abilityToWorkOther = null,
      reasonToNotGetWork = null,
      reasonToNotGetWorkOther = null,
    )
    shortDelayForTimestampChecking()

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
      .hasPrisonId("BXI")
      .hasNoReasonToNotGetWorkOther()
      .hasNoAbilityToWorkOther()
      .hasAbilityToWork(emptySet())
      .hasReasonToNotGetWork(emptySet())
      .wasModifiedBy("auser_gen")
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAfter(persistedInduction.modifiedDateTime)
    // check last modified dates of child objects
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with no qualifications`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      qualificationsAndTraining = aValidUpdateEducationAndQualificationsRequest(
        id = persistedInduction.qualificationsAndTraining!!.id!!,
        educationLevel = null,
        qualifications = emptySet(),
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
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime) // should be unchanged
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with qualifications given it previously did not have any`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(
      prisonNumber,
      aValidCreateCiagInductionRequest(
        qualificationsAndTraining = aValidCreateEducationAndQualificationsRequest(
          educationLevel = null,
          qualifications = null,
          additionalTraining = setOf(TrainingType.OTHER),
          additionalTrainingOther = "Any training",
        ),
      ),
    )
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      qualificationsAndTraining = aValidUpdateEducationAndQualificationsRequest(
        id = persistedInduction.qualificationsAndTraining!!.id,
        educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
        qualifications = setOf(
          aValidAchievedQualification(),
        ),
        additionalTraining = setOf(TrainingType.OTHER),
        additionalTrainingOther = "Any training",
      ),
    )

    val expectedQualificationsAndTraining = aValidEducationAndQualificationsResponse(
      id = persistedInduction.qualificationsAndTraining!!.id!!,
      educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = setOf(
        aValidAchievedQualification(),
      ),
      additionalTraining = setOf(TrainingType.OTHER),
      additionalTrainingOther = "Any training",
      modifiedBy = "auser_gen",
    )
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime)
    assertThat(updatedInduction.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isAfter(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with no previous work experience or work interests`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      workExperience = aValidUpdatePreviousWorkRequest(
        id = persistedInduction.workExperience!!.id!!,
        hasWorkedBefore = false,
        typeOfWorkExperience = null,
        typeOfWorkExperienceOther = null,
        workExperience = null,
        workInterests = aValidUpdateWorkInterestsRequest(
          id = persistedInduction.workExperience!!.workInterests!!.id,
          workInterests = null,
          workInterestsOther = null,
          particularJobInterests = null,
        ),
      ),
    )

    val expectedWorkExperience = aValidPreviousWorkResponse(
      id = persistedInduction.workExperience!!.id!!,
      hasWorkedBefore = false,
      typeOfWorkExperience = null,
      typeOfWorkExperienceOther = null,
      workExperience = null,
      workInterests = aValidWorkInterestsResponse(
        id = persistedInduction.workExperience!!.workInterests!!.id,
        workInterests = emptySet(),
        workInterestsOther = null,
        particularJobInterests = emptySet(),
      ),
      modifiedBy = "auser_gen",
    )
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime) // should be unchanged
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isAfter(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with previous work experience and future interests given it previously did not have any`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(
      prisonNumber,
      aValidCreateCiagInductionRequest(
        workExperience = null,
      ),
    )
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      workExperience = aValidUpdatePreviousWorkRequest(
        hasWorkedBefore = true,
        typeOfWorkExperience = setOf(WorkType.OTHER),
        typeOfWorkExperienceOther = "Scientist",
        workExperience = setOf(aValidWorkExperienceResource()),
        workInterests = aValidUpdateWorkInterestsRequest(),
      ),
    )

    val expectedWorkExperience = aValidPreviousWorkResponse(
      hasWorkedBefore = true,
      typeOfWorkExperience = setOf(WorkType.OTHER),
      typeOfWorkExperienceOther = "Scientist",
      workExperience = setOf(aValidWorkExperienceResource()),
      workInterests = aValidWorkInterestsResponse(),
      modifiedBy = "auser_gen",
    )
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime) // should be unchanged
    assertThat(updatedInduction.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isAfter(persistedInduction.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isAfter(persistedInduction.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with no skills or interests`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
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
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime) // should be unchanged
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(persistedInduction.skillsAndInterests!!.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with skills and interests given it previously did not have any`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest(skillsAndInterests = null))
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      skillsAndInterests = aValidUpdateSkillsAndInterestsRequest(
        skills = setOf(PersonalSkill.OTHER),
        skillsOther = "Hidden skills",
        personalInterests = setOf(PersonalInterest.OTHER),
        personalInterestsOther = "Secret interests",
      ),
    )

    val expectedSkillsAndInterests = aValidSkillsAndInterestsResponse(
      skills = setOf(PersonalSkill.OTHER),
      skillsOther = "Hidden skills",
      personalInterests = setOf(PersonalInterest.OTHER),
      personalInterestsOther = "Secret interests",
      modifiedBy = "auser_gen",
    )
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isAfter(persistedInduction.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isEqualTo(persistedInduction.inPrisonInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with no in prison interests`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
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
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime) // should be unchanged
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(persistedInduction.inPrisonInterests!!.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
  }

  @Test
  fun `should update induction with in-prison interests given it previously did not have any`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateCiagInductionRequest(inPrisonInterests = null))
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestBasedOn(
      originalInduction = persistedInduction,
      inPrisonInterests = aValidUpdatePrisonWorkAndEducationRequest(
        id = null,
        inPrisonWork = setOf(InPrisonWorkType.OTHER),
        inPrisonWorkOther = "Any in-prison work",
        inPrisonEducation = setOf(InPrisonTrainingType.OTHER),
        inPrisonEducationOther = "Any in-prison training",
      ),
    )

    val expectedInPrisonInterests = aValidPrisonWorkAndEducationResponse(
      inPrisonWork = setOf(InPrisonWorkType.OTHER),
      inPrisonWorkOther = "Any in-prison work",
      inPrisonEducation = setOf(InPrisonTrainingType.OTHER),
      inPrisonEducationOther = "Any in-prison training",
      modifiedBy = "auser_gen",
    )
    shortDelayForTimestampChecking()

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
      .wasCreatedAt(persistedInduction.createdDateTime)
      .wasLastModifiedAt(persistedInduction.modifiedDateTime)
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    assertThat(updatedInduction.inPrisonInterests!!.modifiedDateTime).isAfter(persistedInduction.modifiedDateTime)
    // other last modified dates should remain unchanged
    assertThat(updatedInduction.qualificationsAndTraining!!.modifiedDateTime).isEqualTo(persistedInduction.qualificationsAndTraining!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.modifiedDateTime)
    assertThat(updatedInduction.workExperience!!.workInterests!!.modifiedDateTime).isEqualTo(persistedInduction.workExperience!!.workInterests!!.modifiedDateTime)
    assertThat(updatedInduction.skillsAndInterests!!.modifiedDateTime).isEqualTo(persistedInduction.skillsAndInterests!!.modifiedDateTime)
  }

  private fun aValidUpdateInductionRequestBasedOn(
    originalInduction: CiagInductionResponse,
    reference: UUID = originalInduction.reference,
    hopingToGetWork: HopingToWork = originalInduction.hopingToGetWork,
    prisonId: String = originalInduction.prisonId!!,
    abilityToWork: Set<AbilityToWorkFactor>? = originalInduction.abilityToWork,
    abilityToWorkOther: String? = originalInduction.abilityToWorkOther,
    reasonToNotGetWork: Set<ReasonNotToWork>? = originalInduction.reasonToNotGetWork,
    reasonToNotGetWorkOther: String? = originalInduction.reasonToNotGetWorkOther,
    workExperience: UpdatePreviousWorkRequest? = aValidUpdatePreviousWorkRequest(
      id = originalInduction.workExperience?.id,
    ),
    skillsAndInterests: UpdateSkillsAndInterestsRequest? = aValidUpdateSkillsAndInterestsRequest(
      id = originalInduction.skillsAndInterests?.id,
    ),
    qualificationsAndTraining: UpdateEducationAndQualificationsRequest? = aValidUpdateEducationAndQualificationsRequest(
      id = originalInduction.qualificationsAndTraining?.id,
    ),
    inPrisonInterests: UpdatePrisonWorkAndEducationRequest? = aValidUpdatePrisonWorkAndEducationRequest(
      id = originalInduction.inPrisonInterests?.id,
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

  private fun shortDelayForTimestampChecking() {
    Thread.sleep(200)
  }
}
