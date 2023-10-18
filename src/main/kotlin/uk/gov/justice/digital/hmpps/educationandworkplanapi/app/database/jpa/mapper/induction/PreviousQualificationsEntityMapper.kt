package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification

@Mapper(
  uses = [
    QualificationEntityMapper::class,
  ],
)
interface PreviousQualificationsEntityMapper {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PreviousQualifications): PreviousQualificationsEntity
}

@Mapper
interface QualificationEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: Qualification): QualificationEntity
}
