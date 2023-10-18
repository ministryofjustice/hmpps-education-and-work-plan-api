package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience

@Mapper(
  uses = [
    WorkExperienceEntityMapper::class,
  ],
)
interface PreviousWorkExperiencesEntityMapper {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PreviousWorkExperiences): PreviousWorkExperiencesEntity
}

@Mapper
interface WorkExperienceEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: WorkExperience): WorkExperienceEntity
}
