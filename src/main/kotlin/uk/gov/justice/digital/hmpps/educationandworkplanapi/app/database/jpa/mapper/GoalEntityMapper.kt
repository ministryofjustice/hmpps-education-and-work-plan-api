package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

@Mapper(
  uses = [
    StepEntityMapper::class,
  ],
)
interface GoalEntityMapper {

  /**
   * Maps the supplied [Goal] into a new un-persisted [GoalEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [GoalEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  fun fromDomainToEntity(goal: Goal): GoalEntity

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  fun fromEntityToDomain(goalEntity: GoalEntity): Goal
}
