package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.service.EmployabilitySkillsService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education.EducationResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionHistoryScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CompletedActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.ReviewScheduleHistoryResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.sar.SarGoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Service
class SubjectAccessRequestService(
  private val inductionService: InductionService,
  private val employabilitySkillsService: EmployabilitySkillsService,
  private val actionPlanService: ActionPlanService,
  private val noteService: NoteService,
  private val inductionMapper: InductionResourceMapper,
  private val sarGoalMapper: SarGoalResourceMapper,
  private val educationService: EducationService,
  private val educationResourceMapper: EducationResourceMapper,
  private val reviewService: ReviewService,
  private val completedActionPlanReviewResponseMapper: CompletedActionPlanReviewResponseMapper,
  private val inductionScheduleService: InductionScheduleService,
  private val inductionScheduleMapper: InductionHistoryScheduleResourceMapper,
  private val reviewScheduleService: ReviewScheduleService,
  private val reviewScheduleMapper: ReviewScheduleHistoryResponseMapper,
) : HmppsPrisonSubjectAccessRequestService {
  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val fromDateInstance = fromDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
    val toDateInstance = toDate?.atEndOfDay()?.toInstant(ZoneOffset.UTC)

    val goals = try {
      actionPlanService.getActionPlan(prn).goals
        .filter { it.createdAt?.inRange(fromDateInstance, toDateInstance) ?: true }
    } catch (e: ActionPlanNotFoundException) {
      emptyList()
    }

    val induction = try {
      inductionService.getInductionForPrisoner(prn)
        .takeIf { it.createdAt?.inRange(fromDateInstance, toDateInstance) ?: true }
    } catch (e: InductionNotFoundException) {
      null
    }

    val education = try {
      educationService.getPreviousQualificationsForPrisoner(prn)
        // only return the education record if it between the dates if specified
        .takeIf { it.createdAt.inRange(fromDateInstance, toDateInstance) }
        ?.let { education ->
          education.copy(
            // only return the individual qualifications within the education if they are between the dates if specified
            qualifications = education.qualifications
              .filter { it.createdAt.inRange(fromDateInstance, toDateInstance) },
          )
        }
    } catch (e: EducationNotFoundException) {
      null
    }

    val inductionScheduleHistory = inductionScheduleService.getInductionScheduleHistoryForPrisoner(prn)
      .filter { it.createdAt.inRange(fromDateInstance, toDateInstance) }

    val reviewScheduleHistory = reviewScheduleService.getReviewSchedulesForPrisoner(prn)
      .filter { it.createdAt.inRange(fromDateInstance, toDateInstance) }

    val completedReviews = reviewService.getCompletedReviewsForPrisoner(prn)
      .filter { it.createdAt.inRange(fromDateInstance, toDateInstance) }

    val employabilitySkills = employabilitySkillsService.getEmployabilitySkills(prn)
      .filter { it.createdAt.inRange(fromDateInstance, toDateInstance) }

    return if (goals.isEmpty() &&
      induction == null &&
      education == null &&
      inductionScheduleHistory.isEmpty() &&
      reviewScheduleHistory.isEmpty() &&
      completedReviews.isEmpty() &&
      employabilitySkills.isEmpty()
    ) {
      null
    } else {
      HmppsSubjectAccessRequestContent(
        content = SubjectAccessRequestContent(
          induction = induction?.let { inductionMapper.toInductionResponse(induction, employabilitySkills) },
          goals = goals.map {
            val goalNotes = noteService.getNotes(it.reference, EntityType.GOAL)
            sarGoalMapper.fromDomainToModel(it, goalNotes)
          },
          education = education?.let {
            educationResourceMapper.toEducationResponse(it)
          },
          completedReviews = completedReviews.map { completedActionPlanReviewResponseMapper.fromDomainToModel(it) },
          inductionScheduleHistory = inductionScheduleHistory
            .map { inductionScheduleMapper.toInductionResponse(it, induction) }
            .sortedBy { it.version },
          reviewScheduleHistory = reviewScheduleHistory.map { reviewScheduleMapper.fromDomainToModel(it) },
        ),
      )
    }
  }
}

private fun Instant.inRange(from: Instant?, to: Instant?): Boolean = (from == null || this.isEqualOrAfter(from)) &&
  (to == null || this.isEqualOrBefore(to))

private fun Instant.isEqualOrAfter(other: Instant): Boolean = (this == other || isAfter(other))

private fun Instant.isEqualOrBefore(other: Instant): Boolean = (this == other || this.isBefore(other))

private fun LocalDate.atEndOfDay(): LocalDateTime = LocalDateTime.of(this, LocalTime.parse("23:59:59.999"))
