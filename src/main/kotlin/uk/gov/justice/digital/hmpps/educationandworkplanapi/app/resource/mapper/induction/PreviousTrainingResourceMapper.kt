package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationsRequest

@Mapper
interface PreviousTrainingResourceMapper {

  @Mapping(target = "trainingTypes", source = "request.additionalTraining")
  @Mapping(target = "trainingTypeOther", source = "request.additionalTrainingOther")
  fun toCreatePreviousTrainingDto(request: EducationAndQualificationsRequest?, prisonId: String): CreatePreviousTrainingDto?
}
