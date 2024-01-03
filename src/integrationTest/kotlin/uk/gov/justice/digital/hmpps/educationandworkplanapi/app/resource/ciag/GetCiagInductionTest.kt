package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ciag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.isEquivalentTo

class GetCiagInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag/induction/{prisonNumber}"
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
  fun `should get induction for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createCiagInduction(prisonNumber, aValidCreateCiagInductionRequest())
    val expectedWorkExperience = aValidPreviousWorkResponse()
    val expectedSkillsAndInterests = aValidSkillsAndInterestsResponse()
    val expectedQualificationsAndTraining = aValidEducationAndQualificationsResponse()
    val expectedInPrisonInterests = aValidPrisonWorkAndEducationResponse()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(CiagInductionResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForOffenderId(prisonNumber)
      .hasPrisonId("BXI")
      .hasHopingToGetWork(HopingToWork.NOT_SURE)
      .wasCreatedBy("auser_gen")
      .wasModifiedBy("auser_gen")
    assertThat(actual.workExperience).isEquivalentTo(expectedWorkExperience)
    assertThat(actual.skillsAndInterests).isEquivalentTo(expectedSkillsAndInterests)
    assertThat(actual.qualificationsAndTraining).isEquivalentTo(expectedQualificationsAndTraining)
    assertThat(actual.inPrisonInterests).isEquivalentTo(expectedInPrisonInterests)
  }
}
