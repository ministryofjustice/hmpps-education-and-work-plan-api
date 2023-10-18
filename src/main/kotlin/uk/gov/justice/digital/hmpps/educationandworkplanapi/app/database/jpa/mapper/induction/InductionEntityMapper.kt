package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto

@Mapper(
  uses = [
    FutureWorkInterestsEntityMapper::class,
    InPrisonInterestsEntityMapper::class,
    PersonalSkillsAndInterestsEntityMapper::class,
    PreviousQualificationsEntityMapper::class,
    PreviousTrainingEntityMapper::class,
    PreviousWorkExperiencesEntityMapper::class,
    WorkOnReleaseEntityMapper::class,
  ],
)
interface InductionEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun fromDtoToEntity(dto: CreateInductionDto): InductionEntity
}
