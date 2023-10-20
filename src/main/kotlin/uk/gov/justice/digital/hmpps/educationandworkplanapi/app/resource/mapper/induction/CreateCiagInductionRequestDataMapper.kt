package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequestData

@Component
class CreateCiagInductionRequestDataMapper(
  private val workOnReleaseResourceMapper: WorkOnReleaseResourceMapper,
  private val qualificationsResourceMapper: PreviousQualificationsResourceMapper,
  private val trainingResourceMapper: PreviousTrainingResourceMapper,
  private val workExperiencesMapper: PreviousWorkExperiencesMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsMapper,
  private val skillsAndInterestsMapper: PersonalSkillsAndInterestsMapper,
  private val futureWorkInterestsMapper: FutureWorkInterestsMapper,
) {

  fun toCreateInductionDto(prisonNumber: String, request: CreateCiagInductionRequestData): CreateInductionDto {
    // TODO - prisonId is currently optional in the request - need to decided what to do if it's null
    val prisonId = request.prisonId ?: "BXI"
    return CreateInductionDto(
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseResourceMapper.toCreateWorkOnReleaseDto(request),
      previousQualifications = qualificationsResourceMapper.toPreviousQualificationsDto(
        request = request.qualificationsAndTraining,
        prisonId = prisonId,
      ),
      previousTraining = trainingResourceMapper.toPreviousTraining(
        request = request.qualificationsAndTraining,
        prisonId = prisonId,
      ),
      previousWorkExperiences = workExperiencesMapper.toPreviousWorkExperiences(
        request = request.workExperience,
        prisonId = prisonId,
      ),
      inPrisonInterests = inPrisonInterestsMapper.toInPrisonInterests(
        request = request.inPrisonInterests,
        prisonId = prisonId,
      ),
      personalSkillsAndInterests = skillsAndInterestsMapper.toPersonalSkillsAndInterests(
        request = request.skillsAndInterests,
        prisonId = prisonId,
      ),
      futureWorkInterests = futureWorkInterestsMapper.toFutureWorkInterests(
        request = request.workExperience?.workInterests,
        prisonId = prisonId,
      ),
      prisonId = prisonId,
    )
  }
}
