package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationsRequest

@Mapper
interface PreviousQualificationsResourceMapper {
  fun toPreviousQualificationsDto(request: EducationAndQualificationsRequest?, prisonId: String): CreatePreviousQualificationsDto?

  fun toQualification(qualificationRequest: AchievedQualification): Qualification
}
