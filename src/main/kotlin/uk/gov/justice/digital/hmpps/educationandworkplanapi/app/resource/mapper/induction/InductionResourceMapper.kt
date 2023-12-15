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
      previousQualifications = request.previousQualifications?.let {
        qualificationsMapper.toCreatePreviousQualificationsDto(request = it, prisonId = prisonId)
      },
      previousTraining = request.previousTraining?.let {
        previousTrainingMapper.toCreatePreviousTrainingDto(request = it, prisonId = prisonId)
      },
      previousWorkExperiences = request.previousWorkExperiences?.let {
        workExperiencesMapper.toCreatePreviousWorkExperiencesDto(request = it, prisonId = prisonId)
      },
      inPrisonInterests = request.inPrisonInterests?.let {
        inPrisonInterestsMapper.toCreateInPrisonInterestsDto(request = it, prisonId = prisonId)
      },
      personalSkillsAndInterests = request.personalSkillsAndInterests?.let {
        skillsAndInterestsMapper.toCreatePersonalSkillsAndInterestsDto(request = it, prisonId = prisonId)
      },
      futureWorkInterests = request.futureWorkInterests?.let {
        workInterestsMapper.toCreateFutureWorkInterestsDto(request = it, prisonId = prisonId)
      },
      prisonId = prisonId,
    )
  }
}
