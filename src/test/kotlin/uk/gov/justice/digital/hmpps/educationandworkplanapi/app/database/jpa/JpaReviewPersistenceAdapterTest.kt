package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.aValidNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class JpaReviewPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaReviewPersistenceAdapter

  @Mock
  private lateinit var reviewRepository: ReviewRepository

  @Mock
  private lateinit var reviewEntityMapper: ReviewEntityMapper

  @Mock
  private lateinit var noteRepository: NoteRepository

  @Test
  fun `should get completed reviews for a prisoner given prisoner has some reviews`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val reviewEntity1 = aValidReviewEntity()
    val reviewEntity2 = aValidReviewEntity()
    given(reviewRepository.getAllByPrisonNumber(any())).willReturn(
      listOf(reviewEntity1, reviewEntity2),
    )

    val noteEntityForReview1 = aValidNoteEntity(content = "note for review 1")
    val noteEntityForReview2 = aValidNoteEntity(content = "note for review 2")
    given(noteRepository.findAllByEntityReferenceAndEntityType(any(), any())).willReturn(
      listOf(noteEntityForReview1),
      listOf(noteEntityForReview2),
    )

    val completedReview1 = aValidCompletedReview()
    val completedReview2 = aValidCompletedReview()
    given(reviewEntityMapper.fromEntityToDomain(any(), any())).willReturn(completedReview1, completedReview2)

    val expected = listOf(completedReview1, completedReview2)

    // When
    val actual = persistenceAdapter.getCompletedReviews(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(reviewRepository).getAllByPrisonNumber(prisonNumber)
    verify(noteRepository).findAllByEntityReferenceAndEntityType(reviewEntity1.reference, EntityType.REVIEW)
    verify(noteRepository).findAllByEntityReferenceAndEntityType(reviewEntity2.reference, EntityType.REVIEW)
    verify(reviewEntityMapper).fromEntityToDomain(reviewEntity1, noteEntityForReview1)
    verify(reviewEntityMapper).fromEntityToDomain(reviewEntity2, noteEntityForReview2)
  }

  @Test
  fun `should get empty completed reviews for a prisoner given prisoner has no reviews`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    given(reviewRepository.getAllByPrisonNumber(any())).willReturn(emptyList())

    // When
    val actual = persistenceAdapter.getCompletedReviews(prisonNumber)

    // Then
    assertThat(actual).isEmpty()
    verify(reviewRepository).getAllByPrisonNumber(prisonNumber)
    verifyNoInteractions(noteRepository)
    verifyNoInteractions(reviewEntityMapper)
  }

  @Test
  fun `should create completed review`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val createCompletedReviewDto = aValidCreateCompletedReviewDto()
    val deadlineDate = LocalDate.now().plusDays(3)

    val reviewEntity = aValidReviewEntity()
    given(reviewEntityMapper.fromDomainToEntity(any(), any())).willReturn(reviewEntity)
    given(reviewRepository.saveAndFlush(any<ReviewEntity>())).willReturn(reviewEntity)

    val noteEntity = aValidNoteEntity()
    given(noteRepository.saveAndFlush(any<NoteEntity>())).willReturn(noteEntity)

    val expectedCompletedReview = aValidCompletedReview()
    given(reviewEntityMapper.fromEntityToDomain(any(), any())).willReturn(expectedCompletedReview)

    // When
    val actual = persistenceAdapter.createCompletedReview(createCompletedReviewDto, deadlineDate)

    // Then
    assertThat(actual).isEqualTo(expectedCompletedReview)
    verify(reviewEntityMapper).fromDomainToEntity(createCompletedReviewDto, deadlineDate)
    verify(reviewRepository).saveAndFlush(reviewEntity)
    verify(reviewEntityMapper).fromEntityToDomain(reviewEntity, noteEntity)
  }
}
