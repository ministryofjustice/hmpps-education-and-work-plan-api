package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidScheduledActionPlanReviewResponse
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class ScheduledActionPlanReviewResponseMapperTest {
  @InjectMocks
  private lateinit var mapper: ScheduledActionPlanReviewResponseMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map from domain to model`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val reference = aValidReference()
    val reviewSchedule = aValidReviewSchedule(
      reference = reference,
      createdBy = "asmith_gen",
      lastUpdatedBy = "bjones_gen",
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    given(instantMapper.toOffsetDateTime(any())).willReturn(
      reviewSchedule.createdAt.atOffset(ZoneOffset.UTC),
      reviewSchedule.lastUpdatedAt.atOffset(ZoneOffset.UTC),
    )

    val expected = aValidScheduledActionPlanReviewResponse(
      reference = reference,
      createdAt = reviewSchedule.createdAt.atOffset(ZoneOffset.UTC),
      updatedAt = reviewSchedule.lastUpdatedAt.atOffset(ZoneOffset.UTC),
      prisonNumber = prisonNumber,
    )

    // When
    val actual = mapper.fromDomainToModel(reviewSchedule, prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }
}
