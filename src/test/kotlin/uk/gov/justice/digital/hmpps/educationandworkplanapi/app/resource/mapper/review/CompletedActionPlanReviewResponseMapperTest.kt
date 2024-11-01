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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewConductedBy
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note.NoteResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCompletedActionPlanReviewResponse
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class CompletedActionPlanReviewResponseMapperTest {
  @InjectMocks
  private lateinit var mapper: CompletedActionPlanReviewResponseMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Mock
  private lateinit var noteResourceMapper: NoteResourceMapper

  @Test
  fun `should map from model to domain`() {
    // Given
    val reference = aValidReference()
    val note = aValidNoteDto()
    val completedReview = aValidCompletedReview(
      reference = reference,
      note = note,
      createdBy = "asmith_gen",
      conductedBy = ReviewConductedBy("Barnie Jones", "Peer mentor"),
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
    )

    given(instantMapper.toOffsetDateTime(any())).willReturn(completedReview.createdAt.atOffset(ZoneOffset.UTC))

    val expectedNote = aValidNoteResponse()
    given(noteResourceMapper.fromDomainToModel(any())).willReturn(expectedNote)

    val expected = aValidCompletedActionPlanReviewResponse(
      reference = reference,
      note = expectedNote,
      createdAt = completedReview.createdAt.atOffset(ZoneOffset.UTC),
      conductedBy = "Barnie Jones",
      conductedByRole = "Peer mentor",
    )

    // When
    val actual = mapper.fromDomainToModel(completedReview)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(noteResourceMapper).fromDomainToModel(note)
    verify(userService).getUserDetails("asmith_gen")
    verify(instantMapper).toOffsetDateTime(completedReview.createdAt)
  }
}
