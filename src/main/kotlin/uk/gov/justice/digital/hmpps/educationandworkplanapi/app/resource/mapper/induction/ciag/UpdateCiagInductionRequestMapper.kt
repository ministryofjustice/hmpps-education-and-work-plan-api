package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest

@Component
class UpdateCiagInductionRequestMapper(
  private val workOnReleaseMapper: CiagWorkOnReleaseResourceMapper,
  private val qualificationsAndTrainingMapper: QualificationsAndTrainingResourceMapper,
  private val workExperiencesMapper: CiagWorkExperiencesResourceMapper,
  private val inPrisonInterestsMapper: CiagInPrisonInterestsResourceMapper,
  private val skillsAndInterestsMapper: CiagSkillsAndInterestsResourceMapper,
  private val workInterestsMapper: CiagWorkInterestsResourceMapper,
) {

  fun toUpdateInductionDto(prisonNumber: String, request: UpdateCiagInductionRequest): UpdateInductionDto {
    val prisonId = request.prisonId
    return UpdateInductionDto(
      reference = request.reference,
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseMapper.toUpdateWorkOnReleaseDto(request),
      previousQualifications = qualificationsAndTrainingMapper.toUpdatePreviousQualificationsDto(
        request = request.qualificationsAndTraining,
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
