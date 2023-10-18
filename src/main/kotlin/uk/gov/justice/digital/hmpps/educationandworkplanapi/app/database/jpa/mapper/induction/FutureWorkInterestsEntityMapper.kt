package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest

@Mapper(
  uses = [
    WorkInterestEntityMapper::class,
  ],
)
interface FutureWorkInterestsEntityMapper {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: FutureWorkInterests): FutureWorkInterestsEntity
}

@Mapper
interface WorkInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: WorkInterest): WorkInterestEntity
}
