package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class SubjectAccessRequestService(
  private val inductionService: InductionService,
  private val actionPlanService: ActionPlanService,
  private val inductionMapper: InductionResourceMapper,
  private val goalMapper: GoalResourceMapper,
) : HmppsPrisonSubjectAccessRequestService {
  override fun getPrisonContentFor(
    prisonNumber: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val fromDateInstance = fromDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
    val toDateInstance = toDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)

    val goals = actionPlanService.getActionPlan(prisonNumber).goals
      .filter { fromDateInstance == null || it.createdAt?.isAfter(fromDateInstance) ?: true }
      .filter { toDateInstance == null || it.createdAt?.isBefore(toDateInstance) ?: true }

    val induction = try {
      inductionService.getInductionForPrisoner(prisonNumber)
        .takeIf { fromDateInstance == null || it.createdAt?.isAfter(fromDateInstance) ?: true }
        .takeIf { toDateInstance == null || it?.createdAt?.isBefore(toDateInstance) ?: true }
    } catch (e: InductionNotFoundException) {
      null
    }

    if (goals.isEmpty() && induction == null) return null
    return HmppsSubjectAccessRequestContent(
      content = SubjectAccessRequestContent(
        induction = induction?.let { inductionMapper.toInductionResponse(induction) },
        goals = goals.map { goalMapper.fromDomainToModel(it) }.toSet(),
      ),
    )
  }
}
