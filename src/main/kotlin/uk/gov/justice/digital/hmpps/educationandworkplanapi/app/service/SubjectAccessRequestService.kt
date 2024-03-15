package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class SubjectAccessRequestService(
  private val inductionService: InductionService,
  private val actionPlanService: ActionPlanService,
) : HmppsPrisonSubjectAccessRequestService {
  data class SubjectAccessRequestContent(val induction: Induction?, val goals: List<Goal>)

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val fromDateInstance = fromDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
    val toDateInstance = toDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)

    val goals = actionPlanService.getActionPlan(prn).goals
      .filter { fromDateInstance == null || it.createdAt?.isAfter(fromDateInstance) ?: true }
      .filter { toDateInstance == null || it.createdAt?.isBefore(toDateInstance) ?: true }

    val induction = try {
      inductionService.getInductionForPrisoner(prn)
        .takeIf { fromDateInstance == null || it.createdAt?.isAfter(fromDateInstance) ?: true }
        .takeIf { toDateInstance == null || it?.createdAt?.isBefore(toDateInstance) ?: true }
    } catch (e: InductionNotFoundException) {
      null
    }

    if (goals.isEmpty() && induction == null) return null
    return HmppsSubjectAccessRequestContent(
      content = SubjectAccessRequestContent(induction, goals),
    )
  }
}
