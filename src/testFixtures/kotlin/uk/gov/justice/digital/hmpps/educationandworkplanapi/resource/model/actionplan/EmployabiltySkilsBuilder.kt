package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType

fun aValidCreateEmployabilitySkillRequest() = CreateEmployabilitySkillRequest(
  prisonId = "BXI",
  employabilitySkillType = uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType.COMMUNICATION,
  employabilitySkillRating = EmployabilitySkillRating.VERY_CONFIDENT,
  sessionType = EmployabilitySkillSessionType.CIAG_INDUCTION,
  sessionTypeDescription = "Maths class",
  evidence = "evidence",
)
