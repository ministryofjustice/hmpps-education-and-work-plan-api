package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionRequest
import java.time.ZoneOffset

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
        qualificationsMapper.toCreatePreviousQualificationsDto(request = it, prisonNumber = prisonNumber, prisonId = prisonId)
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

  fun toInductionResponse(induction: Induction): InductionResponse {
    with(induction) {
      return InductionResponse(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = workOnReleaseMapper.toWorkOnReleaseResponse(workOnRelease),
        previousQualifications = previousQualifications?.let { qualificationsMapper.toPreviousQualificationsResponse(it) },
        previousTraining = previousTrainingMapper.toPreviousTrainingResponse(previousTraining),
        previousWorkExperiences = previousWorkExperiences?.let { workExperiencesMapper.toPreviousWorkExperiencesResponse(it) },
        inPrisonInterests = inPrisonInterests?.let { inPrisonInterestsMapper.toInPrisonInterestsResponse(it) },
        personalSkillsAndInterests = personalSkillsAndInterests?.let { skillsAndInterestsMapper.toPersonalSkillsAndInterestsResponse(it) },
        futureWorkInterests = futureWorkInterests?.let { workInterestsMapper.toFutureWorkInterestsResponse(it) },
        createdBy = createdBy!!,
        createdByDisplayName = createdByDisplayName!!,
        createdAt = createdAt!!.atOffset(ZoneOffset.UTC),
        createdAtPrison = createdAtPrison,
        updatedBy = lastUpdatedBy!!,
        updatedByDisplayName = lastUpdatedByDisplayName!!,
        updatedAt = lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
        updatedAtPrison = lastUpdatedAtPrison,
      )
    }
  }

  fun toUpdateInductionDto(prisonNumber: String, request: UpdateInductionRequest): UpdateInductionDto {
    val prisonId = request.prisonId
    return UpdateInductionDto(
      reference = request.reference,
      prisonNumber = prisonNumber,
      workOnRelease = request.workOnRelease?.let {
        workOnReleaseMapper.toUpdateWorkOnReleaseDto(request = it, prisonId = prisonId)
      },
      previousQualifications = request.previousQualifications?.let {
        qualificationsMapper.toUpdatePreviousQualificationsDto(
          request = it,
          prisonNumber = prisonNumber,
          prisonId = prisonId,
        )
      },
      previousTraining = request.previousTraining?.let {
        previousTrainingMapper.toUpdatePreviousTrainingDto(request = it, prisonId = prisonId)
      },
      previousWorkExperiences = request.previousWorkExperiences?.let {
        workExperiencesMapper.toUpdatePreviousWorkExperiencesDto(request = it, prisonId = prisonId)
      },
      inPrisonInterests = request.inPrisonInterests?.let {
        inPrisonInterestsMapper.toUpdateInPrisonInterestsDto(request = it, prisonId = prisonId)
      },
      personalSkillsAndInterests = request.personalSkillsAndInterests?.let {
        skillsAndInterestsMapper.toUpdatePersonalSkillsAndInterestsDto(request = it, prisonId = prisonId)
      },
      futureWorkInterests = request.futureWorkInterests?.let {
        workInterestsMapper.toUpdateFutureWorkInterestsDto(request = it, prisonId = prisonId)
      },
      prisonId = prisonId,
    )
  }
}
