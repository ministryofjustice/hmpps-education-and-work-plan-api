package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.capture
import org.mockito.kotlin.firstValue
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPersonalSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkExperiencesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkOnReleaseRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.isEquivalentTo
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class UpdateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}"
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
      .withBody(aValidUpdateInductionRequest())
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
    val updateInductionRequest = aValidUpdateInductionRequest()

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
  fun `should update induction for prisoner who is no longer looking to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createUsername = "auser_gen"
    val createDisplayName = "Albert User"
    val updateUsername = "buser_gen"
    val updateDisplayName = "Bernie User"
    createInduction(
      username = createUsername,
      displayName = createDisplayName,
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
    )
    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestForPrisonerNotLookingToWork(
      reference = persistedInduction.reference,
      prisonId = "MDI",
      workOnRelease = aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork(
        reference = persistedInduction.workOnRelease.reference,
      ),
      inPrisonInterests = aValidUpdateInPrisonInterestsRequest(),
      // null means these values won't be changed
      previousQualifications = null,
      previousTraining = null,
      previousWorkExperiences = null,
      personalSkillsAndInterests = null,
      futureWorkInterests = null,
    )

    // the response fields match the original create request ones, except for the auto generated fields
    val expectedUnchangedWorkExperience = aValidPreviousWorkExperiencesResponse(
      reference = persistedInduction.previousWorkExperiences!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.previousWorkExperiences!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.previousWorkExperiences!!.updatedAt,
    )
    val expectedUnchangedSkillsAndInterests = aValidPersonalSkillsAndInterestsResponse(
      reference = persistedInduction.personalSkillsAndInterests!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.personalSkillsAndInterests!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.personalSkillsAndInterests!!.updatedAt,
    )
    val expectedUnchangedQualifications = aValidPreviousQualificationsResponse(
      reference = persistedInduction.previousQualifications!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.previousQualifications!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.previousQualifications!!.updatedAt,
    )
    val expectedUnchangedTraining = aValidPreviousTrainingResponse(
      reference = persistedInduction.previousTraining!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.previousTraining!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.previousTraining!!.updatedAt,
    )
    val expectedUnchangedWorkInterests = aValidFutureWorkInterestsResponse(
      reference = persistedInduction.futureWorkInterests!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.futureWorkInterests!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.futureWorkInterests!!.updatedAt,
    )
    // only workOnRelease and inPrisonInterests should be different
    val expectedWorkOnRelease = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork(
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAtPrison = "BXI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      // different prison to the create request
      updatedAtPrison = "MDI",
    )
    val expectedInPrisonInterests = aValidInPrisonInterestsResponse(
      // in-prison interests didn't exist previously, so will be created by the update request
      createdBy = updateUsername,
      createdByDisplayName = updateDisplayName,
      createdAtPrison = "MDI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAtPrison = "MDI",
    )
    shortDelayForTimestampChecking()

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = updateUsername,
          displayName = updateDisplayName,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .wasCreatedAt(persistedInduction.createdAt)
      .wasUpdatedAfter(persistedInduction.updatedAt)
      .wasCreatedBy(createUsername)
      .wasCreatedByDisplayName(createDisplayName)
      .wasUpdatedBy(updateUsername)
      .wasUpdatedByDisplayName(updateDisplayName)
      .wasCreatedAtPrison("BXI")
      .wasUpdatedAtPrison("MDI")
    assertThat(updatedInduction.previousWorkExperiences).isEqualTo(expectedUnchangedWorkExperience)
    assertThat(updatedInduction.personalSkillsAndInterests).isEqualTo(expectedUnchangedSkillsAndInterests)
    assertThat(updatedInduction.previousQualifications).isEqualTo(expectedUnchangedQualifications)
    assertThat(updatedInduction.previousTraining).isEqualTo(expectedUnchangedTraining)
    assertThat(updatedInduction.futureWorkInterests).isEqualTo(expectedUnchangedWorkInterests)
    // for the updated objects, we do not have the auto generated values, so use isEquivalentTo()
    assertThat(updatedInduction.workOnRelease).isEquivalentTo(expectedWorkOnRelease)
    assertThat(updatedInduction.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
    assertThat(updatedInduction.workOnRelease.updatedAt).isAfter(persistedInduction.createdAt)
    assertThat(updatedInduction.inPrisonInterests!!.updatedAt).isAfter(persistedInduction.createdAt)

    val timeline = getTimeline(prisonNumber)
    assertThat(timeline)
      .event(2) { // the 2nd Timeline event will be the INDUCTION_UPDATED event
        it.hasEventType(TimelineEventType.INDUCTION_UPDATED)
          .wasActionedBy(updateUsername)
          .hasActionedByDisplayName(updateDisplayName)
      }

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_UPDATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val updateInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(updateInductionEventProperties)
        .containsEntry("prisonId", "MDI")
        .containsEntry("userId", updateUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should update induction for prisoner who is now looking to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createUsername = "auser_gen"
    val createDisplayName = "Albert User"
    val updateUsername = "buser_gen"
    val updateDisplayName = "Bernie User"
    createInduction(
      username = createUsername,
      displayName = createDisplayName,
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(),
    )

    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestForPrisonerLookingToWork(
      reference = persistedInduction.reference,
      prisonId = "MDI",
      workOnRelease = aValidUpdateWorkOnReleaseRequestForPrisonerLookingToWork(
        reference = persistedInduction.workOnRelease.reference,
      ),
      previousQualifications = aValidUpdatePreviousQualificationsRequest(
        reference = persistedInduction.previousQualifications!!.reference,
        educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      ),
      previousWorkExperiences = aValidUpdatePreviousWorkExperiencesRequest(),
      personalSkillsAndInterests = aValidUpdatePersonalSkillsAndInterestsRequest(),
      futureWorkInterests = aValidUpdateFutureWorkInterestsRequest(),
      // these fields were provided in the create request and won't be changed
      inPrisonInterests = null,
      previousTraining = null,
    )

    // the response builder fields match the original create induction builder ones, except for the auto generated fields
    val expectedUnchangedTraining = aValidPreviousTrainingResponse(
      reference = persistedInduction.previousTraining!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.previousTraining!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.previousTraining!!.updatedAt,
    )
    val expectedUnchangedInPrisonInterests = aValidInPrisonInterestsResponse(
      reference = persistedInduction.inPrisonInterests!!.reference,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = persistedInduction.inPrisonInterests!!.createdAt,
      updatedBy = createUsername,
      updatedByDisplayName = createDisplayName,
      updatedAt = persistedInduction.inPrisonInterests!!.updatedAt,
    )
    // these fields should have been modified
    val expectedWorkOnRelease = aValidWorkOnReleaseResponseForPrisonerLookingToWork(
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAtPrison = "BXI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      // different prison to the create request
      updatedAtPrison = "MDI",
    )
    val expectedPreviousQualifications = aValidPreviousQualificationsResponse(
      educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      // didn't exist previously, so will be created by the update request
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAtPrison = "BXI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAtPrison = "MDI",
    )
    // these fields should be created
    val expectedPreviousWorkExperiences = aValidPreviousWorkExperiencesResponse(
      createdBy = updateUsername,
      createdByDisplayName = updateDisplayName,
      createdAtPrison = "MDI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAtPrison = "MDI",
    )
    val expectedPersonalSkillsAndInterests = aValidPersonalSkillsAndInterestsResponse(
      createdBy = updateUsername,
      createdByDisplayName = updateDisplayName,
      createdAtPrison = "MDI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAtPrison = "MDI",
    )
    val expectedFutureWorkInterests = aValidFutureWorkInterestsResponse(
      createdBy = updateUsername,
      createdByDisplayName = updateDisplayName,
      createdAtPrison = "MDI",
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAtPrison = "MDI",
    )
    shortDelayForTimestampChecking()

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = updateUsername,
          displayName = updateDisplayName,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val updatedInduction = getInduction(prisonNumber)
    assertThat(updatedInduction)
      .hasReference(persistedInduction.reference)
      .wasCreatedAt(persistedInduction.createdAt)
      .wasUpdatedAfter(persistedInduction.updatedAt)
      .wasCreatedBy(createUsername)
      .wasCreatedByDisplayName(createDisplayName)
      .wasUpdatedBy(updateUsername)
      .wasUpdatedByDisplayName(updateDisplayName)
      .wasCreatedAtPrison("BXI")
      .wasUpdatedAtPrison("MDI")
    assertThat(updatedInduction.previousTraining).isEqualTo(expectedUnchangedTraining)
    assertThat(updatedInduction.inPrisonInterests).isEqualTo(expectedUnchangedInPrisonInterests)
    // for the updated objects, we do not have the auto generated values, so use isEquivalentTo()
    assertThat(updatedInduction.workOnRelease).isEquivalentTo(expectedWorkOnRelease)
    assertThat(updatedInduction.previousQualifications).isEquivalentTo(expectedPreviousQualifications)
    assertThat(updatedInduction.previousWorkExperiences).isEquivalentTo(expectedPreviousWorkExperiences)
    assertThat(updatedInduction.personalSkillsAndInterests).isEquivalentTo(expectedPersonalSkillsAndInterests)
    assertThat(updatedInduction.futureWorkInterests).isEquivalentTo(expectedFutureWorkInterests)
    assertThat(updatedInduction.workOnRelease.updatedAt).isAfter(persistedInduction.createdAt)
    assertThat(updatedInduction.previousQualifications!!.updatedAt).isAfter(persistedInduction.createdAt)
    assertThat(updatedInduction.previousWorkExperiences!!.updatedAt).isAfter(persistedInduction.createdAt)
    assertThat(updatedInduction.personalSkillsAndInterests!!.updatedAt).isAfter(persistedInduction.createdAt)
    assertThat(updatedInduction.futureWorkInterests!!.updatedAt).isAfter(persistedInduction.createdAt)

    val timeline = getTimeline(prisonNumber)
    assertThat(timeline)
      .event(2) { // the 2nd Timeline event will be the INDUCTION_UPDATED event
        it.hasEventType(TimelineEventType.INDUCTION_UPDATED)
          .wasActionedBy(updateUsername)
          .hasActionedByDisplayName(updateDisplayName)
      }

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_UPDATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val updateInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(updateInductionEventProperties)
        .containsEntry("prisonId", "MDI")
        .containsEntry("userId", updateUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should update induction for prisoner who wants to set their previous work experience as Not Relevant`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(
      prisonNumber = prisonNumber,
      createInductionRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(),
    )

    val persistedInduction = getInduction(prisonNumber)
    val updateInductionRequest = aValidUpdateInductionRequestForPrisonerNotLookingToWork(
      reference = persistedInduction.reference,
      previousWorkExperiences = aValidUpdatePreviousWorkExperiencesRequest(
        hasWorkedBefore = HasWorkedBefore.NOT_RELEVANT,
        hasWorkedBeforeNotRelevantReason = "Prisoner is not looking for work so feels previous work experience is not relevant",
        experiences = emptyList(),
      ),
      workOnRelease = aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork(
        reference = persistedInduction.workOnRelease.reference,
      ),
      previousQualifications = aValidUpdatePreviousQualificationsRequest(
        reference = persistedInduction.previousQualifications!!.reference,
        educationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
      ),
      personalSkillsAndInterests = aValidUpdatePersonalSkillsAndInterestsRequest(),
      futureWorkInterests = aValidUpdateFutureWorkInterestsRequest(),
      // these fields were provided in the create request and won't be changed
      inPrisonInterests = null,
      previousTraining = null,
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(updateInductionRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(privateKey = keyPair.private),
      )
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .previousWorkExperiences {
        it.hasWorkedBefore(HasWorkedBefore.NOT_RELEVANT)
          .hasWorkedBeforeNotRelevantReason("Prisoner is not looking for work so feels previous work experience is not relevant")
      }
  }

  private fun shortDelayForTimestampChecking() {
    Thread.sleep(200)
  }
}
