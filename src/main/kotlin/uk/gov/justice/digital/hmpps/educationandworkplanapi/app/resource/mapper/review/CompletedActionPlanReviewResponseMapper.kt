package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note.NoteResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompletedActionPlanReviewResponse

@Component
class CompletedActionPlanReviewResponseMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
  private val noteResourceMapper: NoteResourceMapper,
) {

  fun fromDomainToModel(completedReview: CompletedReview): CompletedActionPlanReviewResponse =
    with(completedReview) {
      CompletedActionPlanReviewResponse(
        reference = reference,
        completedDate = completedDate,
        deadlineDate = deadlineDate,
        note = noteResourceMapper.fromDomainToModel(note),
        conductedBy = conductedBy?.name ?: userService.getUserDetails(updatedBy).name,
        conductedByRole = conductedBy?.role ?: "CIAG",
        createdBy = createdBy,
        createdByDisplayName = userService.getUserDetails(createdBy).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        createdAtPrison = createdAtPrison,
        reviewScheduleReference = reviewScheduleReference,
      )
    }
}
