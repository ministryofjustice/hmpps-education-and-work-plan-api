package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest

@Component
class InductionResourceMapper(
  private val workOnReleaseMapper: WorkOnReleaseResourceMapper,
  private val qualificationsMapper: QualificationsResourceMapper,
  private val previousTrainingMapper: PreviousTrainingResourceMapper,
  private val workExperiencesMapper: WorkExperiencesResourceMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsResourceMapper,
  private val skillsAndInterestsMapper: SkillsAndInterestsResourceMapper,
  private val workInterestsMapper: WorkInterestsResourceMapper,
) {

  fun toCreateInductionDto(prisonNumber: String, request: CreateInductionRequest): CreateInductionDto {
    val prisonId = request.prisonId
    return CreateInductionDto(
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseMapper.toCreateWorkOnReleaseDto(request.workOnRelease, prisonId),
      previousQualifications = qualificationsMapper
        .toCreatePreviousQualificationsDto(request = request.previousQualifications, prisonId = prisonId),
      previousTraining = previousTrainingMapper
        .toCreatePreviousTrainingDto(request = request.previousTraining, prisonId = prisonId),
      previousWorkExperiences = workExperiencesMapper
        .toCreatePreviousWorkExperiencesDto(request = request.previousWorkExperiences, prisonId = prisonId),
      inPrisonInterests = inPrisonInterestsMapper
        .toCreateInPrisonInterestsDto(request = request.inPrisonInterests, prisonId = prisonId),
      personalSkillsAndInterests = skillsAndInterestsMapper
        .toCreatePersonalSkillsAndInterestsDto(request = request.personalSkillsAndInterests, prisonId = prisonId),
      futureWorkInterests = workInterestsMapper
        .toCreateFutureWorkInterestsDto(request = request.futureWorkInterests, prisonId = prisonId),
      prisonId = prisonId,
    )
  }
}
