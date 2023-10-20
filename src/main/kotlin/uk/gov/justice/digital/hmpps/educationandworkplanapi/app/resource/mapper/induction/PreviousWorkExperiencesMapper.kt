package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience as WorkExperienceDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkExperience as WorkExperienceModel

@Mapper
interface PreviousWorkExperiencesMapper {
  @Mapping(target = "experiences", source = "request.workExperience")
  fun toPreviousWorkExperiences(request: PreviousWorkRequest?, prisonId: String): CreatePreviousWorkExperiencesDto?

  @Mapping(target = "experienceType", source = "typeOfWorkExperience")
  @Mapping(target = "experienceTypeOther", source = "otherWork")
  fun toWorkExperience(request: WorkExperienceModel?): WorkExperienceDomain?
}
