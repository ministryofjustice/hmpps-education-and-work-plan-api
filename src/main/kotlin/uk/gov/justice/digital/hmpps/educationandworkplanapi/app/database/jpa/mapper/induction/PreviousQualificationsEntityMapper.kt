package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousQualificationsDto

@Mapper(
  uses = [
    QualificationEntityMapper::class,
  ],
)
interface PreviousQualificationsEntityMapper {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun fromDtoToEntity(dto: CreatePreviousQualificationsDto): PreviousQualificationsEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  fun fromEntityToDomain(persistedEntity: PreviousQualificationsEntity): PreviousQualifications
}

@Mapper
interface QualificationEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: Qualification): QualificationEntity

  fun fromEntityToDomain(persistedEntity: QualificationEntity): Qualification
}
