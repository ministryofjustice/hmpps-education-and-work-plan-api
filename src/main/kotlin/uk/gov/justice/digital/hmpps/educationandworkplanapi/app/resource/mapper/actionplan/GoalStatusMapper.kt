package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as GoalStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi

fun toGoalStatus(status: GoalStatusApi): GoalStatusDomain = when (status) {
  GoalStatusApi.ACTIVE -> GoalStatusDomain.ACTIVE
  GoalStatusApi.COMPLETED -> GoalStatusDomain.COMPLETED
  GoalStatusApi.ARCHIVED -> GoalStatusDomain.ARCHIVED
}

fun toGoalStatus(status: GoalStatusDomain): GoalStatusApi = when (status) {
  GoalStatusDomain.ACTIVE -> GoalStatusApi.ACTIVE
  GoalStatusDomain.COMPLETED -> GoalStatusApi.COMPLETED
  GoalStatusDomain.ARCHIVED -> GoalStatusApi.ARCHIVED
}
