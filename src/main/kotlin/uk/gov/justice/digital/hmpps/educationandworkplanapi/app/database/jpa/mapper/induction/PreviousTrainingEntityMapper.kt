package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousTraining

@Mapper
interface PreviousTrainingEntityMapper {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PreviousTraining): PreviousTrainingEntity
}
