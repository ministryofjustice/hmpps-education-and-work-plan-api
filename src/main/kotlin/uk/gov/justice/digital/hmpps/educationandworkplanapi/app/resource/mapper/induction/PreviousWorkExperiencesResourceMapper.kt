package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType.OTHER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience as WorkExperienceDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkExperience as WorkExperienceApi

@Mapper
abstract class PreviousWorkExperiencesResourceMapper {
  @Mapping(target = "experiences", source = "request.workExperience")
  abstract fun toCreatePreviousWorkExperiencesDto(
    request: CreatePreviousWorkRequest?,
    prisonId: String,
  ): CreatePreviousWorkExperiencesDto?

  @Mapping(target = "experienceType", source = "typeOfWorkExperience")
  @Mapping(target = "experienceTypeOther", source = "otherWork")
  abstract fun toWorkExperience(request: WorkExperienceApi?): WorkExperienceDomain?

  fun toPreviousWorkResponse(
    workExperiences: PreviousWorkExperiences?,
    workInterests: FutureWorkInterests?,
  ): PreviousWorkResponse? {
    return workExperiences?.let {
      PreviousWorkResponse(
        // In the CIAG API, workInterests are effectively a child of workExperience, so we use the latter as the parent/primary object.
        id = workExperiences.reference,
        hasWorkedBefore = workExperiences.experiences.isNotEmpty(),
        typeOfWorkExperience = workExperiences.experiences.map { toWorkTypeApi(it.experienceType) }.toSet(),
        typeOfWorkExperienceOther = toTypeOfWorkExperienceOther(workExperiences),
        workExperience = workExperiences.experiences.map { toWorkExperienceApi(it) }.toSet(),
        workInterests = toWorkInterestsResponse(workInterests),
        modifiedBy = workExperiences.lastUpdatedBy!!,
        modifiedDateTime = toOffsetDateTime(workExperiences.lastUpdatedAt)!!,
      )
    }
  }

  @Mapping(target = "typeOfWorkExperience", source = "experienceType")
  @Mapping(target = "otherWork", source = "experienceTypeOther")
  abstract fun toWorkExperienceApi(workExperience: WorkExperienceDomain): WorkExperienceApi

  private fun toWorkInterestsResponse(workInterests: FutureWorkInterests?): WorkInterests? {
    return workInterests?.let {
      WorkInterests(
        id = workInterests.reference,
        workInterests = workInterests.interests.map { toWorkTypeApi(it.workType) }.toSet(),
        workInterestsOther = toWorkInterestsOther(workInterests),
        particularJobInterests = workInterests.interests.map { toWorkInterestDetail(it) }.toSet(),
      )
    }
  }

  private fun toWorkInterestDetail(workInterest: WorkInterest): WorkInterestDetail =
    with(workInterest) {
      WorkInterestDetail(
        workInterest = toWorkTypeApi(workType),
        role = role,
      )
    }

  abstract fun toWorkTypeApi(workInterestType: WorkInterestType): WorkType

  abstract fun toWorkTypeApi(workExperienceType: WorkExperienceType): WorkType

  private fun toTypeOfWorkExperienceOther(workExperiences: PreviousWorkExperiences) =
    workExperiences.experiences.firstOrNull { it.experienceType == OTHER }?.experienceTypeOther

  private fun toWorkInterestsOther(workInterests: FutureWorkInterests) =
    workInterests.interests.firstOrNull { it.workType == WorkInterestType.OTHER }?.workTypeOther

  fun toOffsetDateTime(instant: Instant?): OffsetDateTime? = instant?.atOffset(ZoneOffset.UTC)

  @Mapping(target = "reference", source = "request.id")
  @Mapping(target = "experiences", source = "request.workExperience")
  abstract fun toUpdatePreviousWorkExperiencesDto(
    request: UpdatePreviousWorkRequest?,
    prisonId: String,
  ): UpdatePreviousWorkExperiencesDto?
}
