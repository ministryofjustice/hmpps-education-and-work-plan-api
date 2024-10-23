package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InterestType as InterestTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill as PersonalSkillDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.SkillType as SkillTypeDomain

@Component
class SkillsAndInterestsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toCreatePersonalSkillsAndInterestsDto(
    request: CreatePersonalSkillsAndInterestsRequest,
    prisonId: String,
  ): CreatePersonalSkillsAndInterestsDto =
    with(request) {
      CreatePersonalSkillsAndInterestsDto(
        prisonId = prisonId,
        interests = interests.map { toPersonalInterests(it) },
        skills = skills.map { toPersonalSkills(it) },
      )
    }

  fun toPersonalSkillsAndInterestsResponse(personalSkillsAndInterests: PersonalSkillsAndInterests): PersonalSkillsAndInterestsResponse? =
    with(personalSkillsAndInterests) {
      PersonalSkillsAndInterestsResponse(
        reference = reference,
        createdBy = createdBy!!,
        createdByDisplayName = userService.getUserDetails(createdBy!!).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        createdAtPrison = createdAtPrison,
        updatedBy = lastUpdatedBy!!,
        updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
        updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
        updatedAtPrison = lastUpdatedAtPrison,
        interests = interests.map { toPersonalInterests(it) },
        skills = skills.map { toPersonalSkills(it) },
      )
    }

  fun toUpdatePersonalSkillsAndInterestsDto(
    request: UpdatePersonalSkillsAndInterestsRequest,
    prisonId: String,
  ): UpdatePersonalSkillsAndInterestsDto =
    with(request) {
      UpdatePersonalSkillsAndInterestsDto(
        reference = reference,
        prisonId = prisonId,
        interests = interests.map { toPersonalInterests(it) },
        skills = skills.map { toPersonalSkills(it) },
      )
    }

  private fun toPersonalInterests(personalInterest: PersonalInterest): PersonalInterestDomain =
    with(personalInterest) {
      PersonalInterestDomain(
        interestType = toInterestTypeDomain(interestType),
        interestTypeOther = interestTypeOther,
      )
    }

  private fun toPersonalInterests(personalInterest: PersonalInterestDomain): PersonalInterest =
    with(personalInterest) {
      PersonalInterest(
        interestType = toPersonalInterestType(interestType),
        interestTypeOther = interestTypeOther,
      )
    }

  private fun toPersonalSkills(personalSkill: PersonalSkill): PersonalSkillDomain =
    with(personalSkill) {
      PersonalSkillDomain(skillType = toSkillTypeDomain(skillType), skillTypeOther = skillTypeOther)
    }

  private fun toPersonalSkills(personalSkill: PersonalSkillDomain): PersonalSkill =
    with(personalSkill) {
      PersonalSkill(skillType = toPersonalSkillType(skillType), skillTypeOther = skillTypeOther)
    }

  private fun toInterestTypeDomain(interestType: PersonalInterestType): InterestTypeDomain =
    when (interestType) {
      PersonalInterestType.COMMUNITY -> InterestTypeDomain.COMMUNITY
      PersonalInterestType.CRAFTS -> InterestTypeDomain.CRAFTS
      PersonalInterestType.CREATIVE -> InterestTypeDomain.CREATIVE
      PersonalInterestType.DIGITAL -> InterestTypeDomain.DIGITAL
      PersonalInterestType.KNOWLEDGE_BASED -> InterestTypeDomain.KNOWLEDGE_BASED
      PersonalInterestType.MUSICAL -> InterestTypeDomain.MUSICAL
      PersonalInterestType.OUTDOOR -> InterestTypeDomain.OUTDOOR
      PersonalInterestType.NATURE_AND_ANIMALS -> InterestTypeDomain.NATURE_AND_ANIMALS
      PersonalInterestType.SOCIAL -> InterestTypeDomain.SOCIAL
      PersonalInterestType.SOLO_ACTIVITIES -> InterestTypeDomain.SOLO_ACTIVITIES
      PersonalInterestType.SOLO_SPORTS -> InterestTypeDomain.SOLO_SPORTS
      PersonalInterestType.TEAM_SPORTS -> InterestTypeDomain.TEAM_SPORTS
      PersonalInterestType.WELLNESS -> InterestTypeDomain.WELLNESS
      PersonalInterestType.OTHER -> InterestTypeDomain.OTHER
      PersonalInterestType.NONE -> InterestTypeDomain.NONE
    }

  private fun toPersonalInterestType(interestTypeDomain: InterestTypeDomain): PersonalInterestType =
    when (interestTypeDomain) {
      InterestTypeDomain.COMMUNITY -> PersonalInterestType.COMMUNITY
      InterestTypeDomain.CRAFTS -> PersonalInterestType.CRAFTS
      InterestTypeDomain.CREATIVE -> PersonalInterestType.CREATIVE
      InterestTypeDomain.DIGITAL -> PersonalInterestType.DIGITAL
      InterestTypeDomain.KNOWLEDGE_BASED -> PersonalInterestType.KNOWLEDGE_BASED
      InterestTypeDomain.MUSICAL -> PersonalInterestType.MUSICAL
      InterestTypeDomain.OUTDOOR -> PersonalInterestType.OUTDOOR
      InterestTypeDomain.NATURE_AND_ANIMALS -> PersonalInterestType.NATURE_AND_ANIMALS
      InterestTypeDomain.SOCIAL -> PersonalInterestType.SOCIAL
      InterestTypeDomain.SOLO_ACTIVITIES -> PersonalInterestType.SOLO_ACTIVITIES
      InterestTypeDomain.SOLO_SPORTS -> PersonalInterestType.SOLO_SPORTS
      InterestTypeDomain.TEAM_SPORTS -> PersonalInterestType.TEAM_SPORTS
      InterestTypeDomain.WELLNESS -> PersonalInterestType.WELLNESS
      InterestTypeDomain.OTHER -> PersonalInterestType.OTHER
      InterestTypeDomain.NONE -> PersonalInterestType.NONE
    }

  private fun toSkillTypeDomain(skillType: PersonalSkillType): SkillTypeDomain =
    when (skillType) {
      PersonalSkillType.COMMUNICATION -> SkillTypeDomain.COMMUNICATION
      PersonalSkillType.POSITIVE_ATTITUDE -> SkillTypeDomain.POSITIVE_ATTITUDE
      PersonalSkillType.RESILIENCE -> SkillTypeDomain.RESILIENCE
      PersonalSkillType.SELF_MANAGEMENT -> SkillTypeDomain.SELF_MANAGEMENT
      PersonalSkillType.TEAMWORK -> SkillTypeDomain.TEAMWORK
      PersonalSkillType.THINKING_AND_PROBLEM_SOLVING -> SkillTypeDomain.THINKING_AND_PROBLEM_SOLVING
      PersonalSkillType.WILLINGNESS_TO_LEARN -> SkillTypeDomain.WILLINGNESS_TO_LEARN
      PersonalSkillType.OTHER -> SkillTypeDomain.OTHER
      PersonalSkillType.NONE -> SkillTypeDomain.NONE
    }

  private fun toPersonalSkillType(skillTypeDomain: SkillTypeDomain): PersonalSkillType =
    when (skillTypeDomain) {
      SkillTypeDomain.COMMUNICATION -> PersonalSkillType.COMMUNICATION
      SkillTypeDomain.POSITIVE_ATTITUDE -> PersonalSkillType.POSITIVE_ATTITUDE
      SkillTypeDomain.RESILIENCE -> PersonalSkillType.RESILIENCE
      SkillTypeDomain.SELF_MANAGEMENT -> PersonalSkillType.SELF_MANAGEMENT
      SkillTypeDomain.TEAMWORK -> PersonalSkillType.TEAMWORK
      SkillTypeDomain.THINKING_AND_PROBLEM_SOLVING -> PersonalSkillType.THINKING_AND_PROBLEM_SOLVING
      SkillTypeDomain.WILLINGNESS_TO_LEARN -> PersonalSkillType.WILLINGNESS_TO_LEARN
      SkillTypeDomain.OTHER -> PersonalSkillType.OTHER
      SkillTypeDomain.NONE -> PersonalSkillType.NONE
    }
}
