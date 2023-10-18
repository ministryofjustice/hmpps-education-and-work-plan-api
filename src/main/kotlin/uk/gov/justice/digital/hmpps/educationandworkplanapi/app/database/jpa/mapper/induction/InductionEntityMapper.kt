package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction

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
  fun fromDomainToEntity(domain: Induction): InductionEntity
}
