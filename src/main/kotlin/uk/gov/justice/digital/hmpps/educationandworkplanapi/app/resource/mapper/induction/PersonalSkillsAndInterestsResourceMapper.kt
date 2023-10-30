package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill as PersonalSkillDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest as PersonalInterestApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill as PersonalSkillApi

@Component
class PersonalSkillsAndInterestsResourceMapper(
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

  private fun toPersonalSkillsApi(skills: List<PersonalSkillDomain>): Set<PersonalSkillApi> =
    skills.map { PersonalSkillApi.valueOf(it.skillType.name) }.toSet()

  private fun toPersonalInterestsApi(interests: List<PersonalInterestDomain>): Set<PersonalInterestApi> =
    interests.map { PersonalInterestApi.valueOf(it.interestType.name) }.toSet()

  private fun toSkillsTypeOther(skillsAndInterests: PersonalSkillsAndInterests) =
    skillsAndInterests.skills.firstOrNull { it.skillType == SkillType.OTHER }?.skillTypeOther

  private fun toPersonalInterestsOther(skillsAndInterests: PersonalSkillsAndInterests) =
    skillsAndInterests.interests.firstOrNull { it.interestType == InterestType.OTHER }?.interestTypeOther
}
