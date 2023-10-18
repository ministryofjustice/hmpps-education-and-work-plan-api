package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest

@Mapper(
  uses = [
    InPrisonWorkInterestEntityMapper::class,
    InPrisonTrainingInterestEntityMapper::class,
  ],
)
interface InPrisonInterestsEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: InPrisonInterests): InPrisonInterestsEntity
}

@Mapper
interface InPrisonWorkInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: InPrisonWorkInterest): InPrisonWorkInterestEntity
}

@Mapper
interface InPrisonTrainingInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: InPrisonTrainingInterest): InPrisonTrainingInterestEntity
}
