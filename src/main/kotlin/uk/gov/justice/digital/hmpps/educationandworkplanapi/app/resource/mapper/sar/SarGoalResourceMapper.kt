package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.sar

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.StepResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.toGoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.toReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SarGoalResponse

@Component
class SarGoalResourceMapper(
  private val stepResourceMapper: StepResourceMapper,
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {

  fun fromDomainToModel(goalDomain: Goal, goalDomainNotes: List<NoteDto>): SarGoalResponse = with(goalDomain) {
    SarGoalResponse(
      title = title,
      steps = steps.map { stepResourceMapper.fromDomainToModel(it) },
      targetCompletionDate = targetCompletionDate,
      status = toGoalStatus(status),
      goalNote = goalDomainNotes
        .sortedBy { it.createdAt }
        .findLast { it.noteType == NoteType.GOAL }?.content,
      goalCompletionNote = goalDomainNotes
        .sortedBy { it.createdAt }
        .findLast { it.noteType == NoteType.GOAL_COMPLETION }?.content,
      goalArchiveReason = archiveReason?.let { toReasonToArchiveGoal(it) },
      goalArchiveReasonOther = archiveReasonOther,
      goalArchiveNote = goalDomainNotes
        .sortedBy { it.createdAt }
        .findLast { it.noteType == NoteType.GOAL_ARCHIVAL }?.content,
      createdBy = createdBy!!,
      createdByDisplayName = userService.getUserDetails(createdBy!!).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy!!,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
    )
  }
}
