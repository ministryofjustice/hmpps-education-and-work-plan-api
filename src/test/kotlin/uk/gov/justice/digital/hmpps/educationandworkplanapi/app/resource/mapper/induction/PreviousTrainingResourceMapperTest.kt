package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousTrainingRequest
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType as TrainingTypeApi

@ExtendWith(MockitoExtension::class)
class PreviousTrainingResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: PreviousTrainingResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map to CreatePreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousTrainingRequest()
    val expected = aValidCreatePreviousTrainingDto(
      trainingTypes = listOf(TrainingTypeDomain.OTHER),
      trainingTypeOther = "Certified Kotlin Developer",
    )

    // When
    val actual = mapper.toCreatePreviousTrainingDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PreviousTrainingResponse`() {
    // Given
    val domain = aValidPreviousTraining(
      trainingTypes = listOf(TrainingTypeDomain.OTHER),
      trainingTypeOther = "Certified Kotlin Developer",
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidPreviousTrainingResponse(
      reference = domain.reference,
      trainingTypes = listOf(TrainingTypeApi.OTHER),
      trainingTypeOther = "Certified Kotlin Developer",
      createdAt = expectedDateTime,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toPreviousTrainingResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map to UpdatePreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdatePreviousTrainingRequest()
    val expected = aValidUpdatePreviousTrainingDto(
      reference = request.reference!!,
      trainingTypes = listOf(TrainingTypeDomain.OTHER),
      trainingTypeOther = "Certified Kotlin Developer",
    )

    // When
    val actual = mapper.toUpdatePreviousTrainingDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
