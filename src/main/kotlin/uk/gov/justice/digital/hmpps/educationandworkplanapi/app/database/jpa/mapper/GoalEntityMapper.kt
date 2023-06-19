package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import java.time.Instant

@Mapper(
  uses = [
    StepEntityMapper::class,
  ],
  imports = [
    Instant::class,
  ],
)
interface GoalEntityMapper {

  @Mapping(target = "id", expression = "java( null )")
  fun fromDomainToEntity(goal: Goal): GoalEntity

  @Mapping(target = "createdBy", expression = "java( \"\" )")
  @Mapping(target = "createdAt", expression = "java( Instant.EPOCH )")
  @Mapping(target = "lastUpdatedBy", expression = "java( \"\" )")
  @Mapping(target = "lastUpdatedAt", expression = "java( Instant.EPOCH )")
  fun fromEntityToDomain(goalEntity: GoalEntity): Goal
}
