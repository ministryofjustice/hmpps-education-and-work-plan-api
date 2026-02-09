package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import java.time.LocalDate

fun aValidCreateEmployabilitySkillRequest() = CreateEmployabilitySkillRequest(
  prisonId = "BXI",
  employabilitySkillType = uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType.COMMUNICATION,
  employabilitySkillRating = EmployabilitySkillRating.VERY_CONFIDENT,
  activityName = "Maths class",
  evidence = "evidence",
  conversationDate = LocalDate.now(),
)
