package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ciag

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidQualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingType as InPrisonTrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkType as InPrisonWorkTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity

class CreateCiagInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag/induction/{prisonNumber}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateCiagInductionRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create induction given no induction data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
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
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  @Transactional
  fun `should fail to create induction given induction already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val induction = aValidInductionEntity()
    inductionRepository.save(induction)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    val createRequest = aValidCreateCiagInductionRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("An Induction already exists for prisoner $prisonNumber")
  }

  @Test
  @Transactional
  fun `should create a new induction given prisoner does not have an induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateCiagInductionRequest()
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"
    val prisonId = createRequest.prisonId

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = inductionRepository.findByPrisonNumber(prisonNumber)
    assertThat(induction)
      .isForPrisonNumber(prisonNumber)
      .hasAReference()
      .hasJpaManagedFieldsPopulated()
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(prisonId)
      .wasUpdatedAtPrison(prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  @Transactional
  fun `should create a short route induction`() {
    // Given
    val prisonNumber = "B5678CD"
    val createRequest = aValidCreateCiagInductionRequest(
      hopingToGetWork = HopingToWork.NO,
      abilityToWork = setOf(AbilityToWorkFactor.OTHER),
      abilityToWorkOther = "Lack of interest",
      reasonToNotGetWork = setOf(ReasonNotToWork.OTHER),
      reasonToNotGetWorkOther = "Crime pays",
      workExperience = null,
      skillsAndInterests = null,
      qualificationsAndTraining = aValidCreateEducationAndQualificationsRequest(
        // education level is not set in short route
        educationLevel = null,
        qualifications = setOf(aValidAchievedQualification()),
        additionalTraining = setOf(TrainingType.NONE),
        additionalTrainingOther = null,
      ),
      inPrisonInterests = aValidCreatePrisonWorkAndEducationRequest(
        inPrisonWork = setOf(InPrisonWorkType.OTHER),
        inPrisonWorkOther = "Any in-prison work",
        inPrisonEducation = setOf(InPrisonTrainingType.OTHER),
        inPrisonEducationOther = "Any in-prison training",
      ),
    )
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"
    val prisonId = createRequest.prisonId
    val expectedPreviousQualifications = aValidPreviousQualificationsEntity(
      educationLevel = HighestEducationLevel.NOT_SURE,
      qualifications = mutableListOf(aValidQualificationEntity()),
    )
    val expectedPreviousTraining = aValidPreviousTrainingEntity(
      trainingTypes = mutableListOf(TrainingTypeEntity.NONE),
      trainingTypeOther = null,
    )
    val expectedInPrisonInterests = aValidInPrisonInterestsEntity(
      inPrisonWorkInterests = mutableListOf(
        aValidInPrisonWorkInterestEntity(
          workType = InPrisonWorkTypeEntity.OTHER,
          workTypeOther = "Any in-prison work",
        ),
      ),
      inPrisonTrainingInterests = mutableListOf(
        aValidInPrisonTrainingInterestEntity(
          trainingType = InPrisonTrainingTypeEntity.OTHER,
          trainingTypeOther = "Any in-prison training",
        ),
      ),
    )

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = inductionRepository.findByPrisonNumber(prisonNumber)
    assertThat(induction)
      .isForPrisonNumber(prisonNumber)
      .hasAReference()
      .hasJpaManagedFieldsPopulated()
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(prisonId)
      .wasUpdatedAtPrison(prisonId)
    assertThat(induction!!.previousQualifications).isEqualToIgnoringInternallyManagedFields(expectedPreviousQualifications)
    assertThat(induction.previousTraining).isEqualToIgnoringInternallyManagedFields(expectedPreviousTraining)
    assertThat(induction.inPrisonInterests).isEqualToIgnoringInternallyManagedFields(expectedInPrisonInterests)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  @Transactional
  fun `should create a new induction with empty collections`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateCiagInductionRequest(
      hopingToGetWork = HopingToWork.YES,
      abilityToWork = emptySet(),
      abilityToWorkOther = null,
      reasonToNotGetWork = emptySet(),
      reasonToNotGetWorkOther = null,
      workExperience = aValidCreatePreviousWorkRequest(
        hasWorkedBefore = false,
        workExperience = emptySet(),
        typeOfWorkExperience = emptySet(),
        workInterests = aValidCreateWorkInterestsRequest(
          workInterests = emptySet(),
          workInterestsOther = null,
          particularJobInterests = emptySet(),
        ),
      ),
      skillsAndInterests = aValidCreateSkillsAndInterestsRequest(
        skills = emptySet(),
        skillsOther = null,
        personalInterests = emptySet(),
        personalInterestsOther = null,
      ),
      qualificationsAndTraining = aValidCreateEducationAndQualificationsRequest(
        qualifications = emptySet(),
        // additional training set to an empty collection is not possible via the UI but technically the API supports it so is tested here
        additionalTraining = emptySet(),
        additionalTrainingOther = null,
      ),
      inPrisonInterests = aValidCreatePrisonWorkAndEducationRequest(
        inPrisonWork = emptySet(),
        inPrisonWorkOther = null,
        inPrisonEducation = emptySet(),
        inPrisonEducationOther = null,
      ),
    )
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"
    val prisonId = createRequest.prisonId

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = inductionRepository.findByPrisonNumber(prisonNumber)
    assertThat(induction)
      .isForPrisonNumber(prisonNumber)
      .hasAReference()
      .hasJpaManagedFieldsPopulated()
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(prisonId)
      .wasUpdatedAtPrison(prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  @Transactional
  fun `should create a new induction with null collections`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateCiagInductionRequest(
      hopingToGetWork = HopingToWork.YES,
      abilityToWork = null,
      abilityToWorkOther = null,
      reasonToNotGetWork = null,
      reasonToNotGetWorkOther = null,
      workExperience = aValidCreatePreviousWorkRequest(
        hasWorkedBefore = false,
        workExperience = null,
        typeOfWorkExperience = null,
        workInterests = aValidCreateWorkInterestsRequest(
          workInterests = null,
          workInterestsOther = null,
          particularJobInterests = null,
        ),
      ),
      skillsAndInterests = aValidCreateSkillsAndInterestsRequest(
        skills = null,
        skillsOther = null,
        personalInterests = null,
        personalInterestsOther = null,
      ),
      qualificationsAndTraining = aValidCreateEducationAndQualificationsRequest(
        educationLevel = null,
        qualifications = null,
        // additional training cannot be set to null
        additionalTraining = setOf(TrainingType.NONE),
        additionalTrainingOther = null,
      ),
      inPrisonInterests = aValidCreatePrisonWorkAndEducationRequest(
        inPrisonWork = null,
        inPrisonWorkOther = null,
        inPrisonEducation = null,
        inPrisonEducationOther = null,
      ),
    )
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"
    val prisonId = createRequest.prisonId

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = inductionRepository.findByPrisonNumber(prisonNumber)
    assertThat(induction)
      .isForPrisonNumber(prisonNumber)
      .hasAReference()
      .hasJpaManagedFieldsPopulated()
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(prisonId)
      .wasUpdatedAtPrison(prisonId)
    assertThat(induction!!.previousQualifications!!.educationLevel).isEqualTo(HighestEducationLevel.NOT_SURE)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }
}
