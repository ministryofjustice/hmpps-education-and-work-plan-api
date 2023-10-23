package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequestData

@Component
class CreateCiagInductionRequestDataMapper(
  private val workOnReleaseMapper: WorkOnReleaseResourceMapper,
  private val qualificationsMapper: PreviousQualificationsResourceMapper,
  private val previousTrainingMapper: PreviousTrainingResourceMapper,
  private val workExperiencesMapper: PreviousWorkExperiencesResourceMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsResourceMapper,
  private val skillsAndInterestsMapper: PersonalSkillsAndInterestsResourceMapper,
  private val workInterestsMapper: FutureWorkInterestsResourceMapper,
) {

  fun toCreateInductionDto(prisonNumber: String, request: CreateCiagInductionRequestData): CreateInductionDto {
    val prisonId = request.prisonId
    return CreateInductionDto(
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseMapper.toCreateWorkOnReleaseDto(request),
      previousQualifications = qualificationsMapper.toCreatePreviousQualificationsDto(
        request = request.qualificationsAndTraining,
        prisonId = prisonId,
      ),
      previousTraining = previousTrainingMapper.toCreatePreviousTrainingDto(
        request = request.qualificationsAndTraining,
        prisonId = prisonId,
      ),
      previousWorkExperiences = workExperiencesMapper.toCreatePreviousWorkExperiencesDto(
        request = request.workExperience,
        prisonId = prisonId,
      ),
      inPrisonInterests = inPrisonInterestsMapper.toCreateInPrisonInterestsDto(
        request = request.inPrisonInterests,
        prisonId = prisonId,
      ),
      personalSkillsAndInterests = skillsAndInterestsMapper.toCreatePersonalSkillsAndInterestsDto(
        request = request.skillsAndInterests,
        prisonId = prisonId,
      ),
      futureWorkInterests = workInterestsMapper.toCreateFutureWorkInterestsDto(
        request = request.workExperience?.workInterests,
        prisonId = prisonId,
      ),
      prisonId = prisonId,
    )
  }
}
