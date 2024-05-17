package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InterestType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.SkillType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill as PersonalSkillDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType as PersonalInterestApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType as PersonalSkillApi

@Component
class CiagSkillsAndInterestsResourceMapper(
  private val instantMapper: InstantMapper,
) {
  fun toCreatePersonalSkillsAndInterestsDto(
    request: CreateSkillsAndInterestsRequest?,
    prisonId: String,
  ): CreatePersonalSkillsAndInterestsDto? =
    request?.let {
      CreatePersonalSkillsAndInterestsDto(
        skills = toPersonalSkillsDomain(request.skills, request.skillsOther),
        interests = toPersonalInterestsDomain(request.personalInterests, request.personalInterestsOther),
        prisonId = prisonId,
      )
    }

  fun toSkillsAndInterestsResponse(skillsAndInterests: PersonalSkillsAndInterests?): SkillsAndInterestsResponse? {
    return skillsAndInterests?.let {
      SkillsAndInterestsResponse(
        id = skillsAndInterests.reference,
        skills = toPersonalSkillsApi(skillsAndInterests.skills),
        skillsOther = toSkillsTypeOther(skillsAndInterests),
        personalInterests = toPersonalInterestsApi(skillsAndInterests.interests),
        personalInterestsOther = toPersonalInterestsOther(skillsAndInterests),
        modifiedBy = skillsAndInterests.lastUpdatedBy!!,
        modifiedDateTime = instantMapper.toOffsetDateTime(skillsAndInterests.lastUpdatedAt)!!,
      )
    }
  }

  private fun toPersonalSkillsDomain(skills: Set<PersonalSkillApi>?, skillsOther: String?): List<PersonalSkillDomain> {
    return skills?.map {
      val skillType = SkillType.valueOf(it.name)
      val skillTypeOther = if (it == PersonalSkillApi.OTHER) skillsOther else null
      PersonalSkillDomain(
        skillType = skillType,
        skillTypeOther = skillTypeOther,
      )
    } ?: emptyList()
  }

  private fun toPersonalInterestsDomain(
    interests: Set<PersonalInterestApi>?,
    interestsOther: String?,
  ): List<PersonalInterestDomain> {
    return interests?.map {
      val interestType = InterestType.valueOf(it.name)
      val interestTypeOther = if (it == PersonalInterestApi.OTHER) interestsOther else null
      PersonalInterestDomain(
        interestType = interestType,
        interestTypeOther = interestTypeOther,
      )
    } ?: emptyList()
  }

  private fun toPersonalSkillsApi(skills: List<PersonalSkillDomain>): Set<PersonalSkillApi>? =
    skills.map { PersonalSkillApi.valueOf(it.skillType.name) }.toSet()

  private fun toPersonalInterestsApi(interests: List<PersonalInterestDomain>): Set<PersonalInterestApi>? =
    interests.map { PersonalInterestApi.valueOf(it.interestType.name) }.toSet()

  private fun toSkillsTypeOther(skillsAndInterests: PersonalSkillsAndInterests) =
    skillsAndInterests.skills.firstOrNull { it.skillType == SkillType.OTHER }?.skillTypeOther

  private fun toPersonalInterestsOther(skillsAndInterests: PersonalSkillsAndInterests) =
    skillsAndInterests.interests.firstOrNull { it.interestType == InterestType.OTHER }?.interestTypeOther

  fun toUpdatePersonalSkillsAndInterestsDto(
    request: UpdateSkillsAndInterestsRequest?,
    prisonId: String,
  ): UpdatePersonalSkillsAndInterestsDto? =
    request?.let {
      UpdatePersonalSkillsAndInterestsDto(
        reference = it.id,
        skills = toPersonalSkillsDomain(request.skills, request.skillsOther),
        interests = toPersonalInterestsDomain(request.personalInterests, request.personalInterestsOther),
        prisonId = prisonId,
      )
    }
}
