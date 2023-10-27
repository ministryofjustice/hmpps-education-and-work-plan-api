package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@Component
class CiagInductionResponseMapper(
  private val workOnReleaseMapper: WorkOnReleaseResourceMapper,
  private val qualificationsAndTrainingMapper: QualificationsAndTrainingResourceMapper,
  private val workExperiencesMapper: PreviousWorkExperiencesResourceMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsResourceMapper,
  private val skillsAndInterestsMapper: PersonalSkillsAndInterestsResourceMapper,
  private val instantMapper: InstantMapper,
) {
  fun fromDomainToModel(inductionDomain: Induction): CiagInductionResponse {
    with(inductionDomain) {
      return CiagInductionResponse(
        reference = reference,
        offenderId = prisonNumber,
        prisonId = lastUpdatedAtPrison,
        desireToWork = when (workOnRelease.hopingToWork) {
          HopingToWorkDomain.YES -> true
          else -> false
        },
        hopingToGetWork = HopingToWorkApi.valueOf(workOnRelease.hopingToWork.name),
        reasonToNotGetWork = workOnReleaseMapper.toReasonsNotToWork(workOnRelease.notHopingToWorkReasons),
        reasonToNotGetWorkOther = workOnRelease.notHopingToWorkOtherReason,
        abilityToWork = workOnReleaseMapper.toAbilityToWorkFactors(workOnRelease.affectAbilityToWork),
        abilityToWorkOther = workOnRelease.affectAbilityToWorkOther,
        workExperience = workExperiencesMapper.toPreviousWorkResponse(previousWorkExperiences, futureWorkInterests),
        skillsAndInterests = skillsAndInterestsMapper.toSkillsAndInterestsResponse(personalSkillsAndInterests),
        qualificationsAndTraining = qualificationsAndTrainingMapper.toEducationAndQualificationResponse(
          previousQualifications,
          previousTraining,
        ),
        inPrisonInterests = inPrisonInterestsMapper.toPrisonWorkAndEducationResponse(inPrisonInterests),
        createdBy = createdBy!!,
        createdDateTime = instantMapper.toOffsetDateTime(createdAt)!!,
        modifiedBy = lastUpdatedBy!!,
        modifiedDateTime = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      )
    }
  }
}
