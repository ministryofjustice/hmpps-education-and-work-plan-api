package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkillsAndInterests

@Mapper(
  uses = [
    PersonalSkillEntityMapper::class,
    PersonalInterestEntityMapper::class,
  ],
)
interface PersonalSkillsAndInterestsEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PersonalSkillsAndInterests): PersonalSkillsAndInterestsEntity
}

@Mapper
interface PersonalSkillEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity
}

@Mapper
interface PersonalInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity
}
