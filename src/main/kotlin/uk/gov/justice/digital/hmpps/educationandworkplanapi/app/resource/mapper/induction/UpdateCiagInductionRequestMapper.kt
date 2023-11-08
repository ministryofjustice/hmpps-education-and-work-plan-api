package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest

@Component
class UpdateCiagInductionRequestMapper(
  private val workOnReleaseMapper: WorkOnReleaseResourceMapper,
  private val qualificationsAndTrainingMapper: QualificationsAndTrainingResourceMapper,
  private val workExperiencesMapper: PreviousWorkExperiencesResourceMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsResourceMapper,
  private val skillsAndInterestsMapper: PersonalSkillsAndInterestsResourceMapper,
  private val workInterestsMapper: FutureWorkInterestsResourceMapper,
) {

  fun toUpdateInductionDto(prisonNumber: String, request: UpdateCiagInductionRequest): UpdateInductionDto {
    val prisonId = request.prisonId
    return UpdateInductionDto(
      reference = request.reference,
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseMapper.toUpdateWorkOnReleaseDto(request),
      previousQualifications = qualificationsAndTrainingMapper.toUpdatePreviousQualificationsDto(
        request = request.qualificationsAndTraining!!,
        prisonId = prisonId,
      ),
      previousTraining = qualificationsAndTrainingMapper.toUpdatePreviousTrainingDto(
        request = request.qualificationsAndTraining,
        prisonId = prisonId,
      ),
      previousWorkExperiences = workExperiencesMapper.toUpdatePreviousWorkExperiencesDto(
        request = request.workExperience,
        prisonId = prisonId,
      ),
      inPrisonInterests = inPrisonInterestsMapper.toUpdateInPrisonInterestsDto(
        request = request.inPrisonInterests,
        prisonId = prisonId,
      ),
      personalSkillsAndInterests = skillsAndInterestsMapper.toUpdatePersonalSkillsAndInterestsDto(
        request = request.skillsAndInterests,
        prisonId = prisonId,
      ),
      futureWorkInterests = workInterestsMapper.toUpdateFutureWorkInterestsDto(
        request = request.workExperience?.workInterests,
        prisonId = prisonId,
      ),
      prisonId = prisonId,
    )
  }
}
