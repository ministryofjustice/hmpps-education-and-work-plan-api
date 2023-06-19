package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

@Mapper
interface GoalEntityMapper {

  fun fromDomainToEntity(goal: Goal): GoalEntity

  fun fromEntityToDomain(goalEntity: GoalEntity): Goal
}
