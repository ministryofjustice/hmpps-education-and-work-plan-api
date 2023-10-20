package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill as PersonalSkillDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest as PersonalInterestApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill as PersonalSkillApi

@Component
class PersonalSkillsAndInterestsMapper {
  fun toPersonalSkillsAndInterests(
    request: SkillsAndInterestsRequest?,
    prisonId: String,
  ): CreatePersonalSkillsAndInterestsDto? {
    return request?.let {
      CreatePersonalSkillsAndInterestsDto(
        skills = toPersonalSkills(request.skills, request.skillsOther),
        interests = toPersonalInterests(request.personalInterests, request.personalInterestsOther),
        prisonId = prisonId,
      )
    }
  }

  fun toPersonalSkills(skills: Set<PersonalSkillApi>?, skillsOther: String?): List<PersonalSkillDomain> {
    return skills?.map {
      val skillType = SkillType.valueOf(it.name)
      val skillTypeOther = if (it == PersonalSkillApi.OTHER) skillsOther else null
      PersonalSkillDomain(
        skillType = skillType,
        skillTypeOther = skillTypeOther,
      )
    } ?: emptyList()
  }

  fun toPersonalInterests(interests: Set<PersonalInterestApi>?, interestsOther: String?): List<PersonalInterestDomain> {
    return interests?.map {
      val interestType = InterestType.valueOf(it.name)
      val interestTypeOther = if (it == PersonalInterestApi.OTHER) interestsOther else null
      PersonalInterestDomain(
        interestType = interestType,
        interestTypeOther = interestTypeOther,
      )
    } ?: emptyList()
  }
}
