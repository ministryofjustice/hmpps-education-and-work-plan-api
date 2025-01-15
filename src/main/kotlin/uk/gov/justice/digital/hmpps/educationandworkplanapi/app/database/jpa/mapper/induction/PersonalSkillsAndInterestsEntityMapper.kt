package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InterestType as InterestTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.SkillType as SkillTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InterestType as InterestTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.SkillType as SkillTypeEntity

@Component
class PersonalSkillsAndInterestsEntityMapper(
  private val personalSkillEntityMapper: PersonalSkillEntityMapper,
  private val personalInterestEntityMapper: PersonalInterestEntityMapper,
  private val personalSkillEntityListManager: InductionEntityListManager<PersonalSkillEntity, PersonalSkill>,
  private val personalInterestEntityListManager: InductionEntityListManager<PersonalInterestEntity, PersonalInterest>,
) {
  fun fromCreateDtoToEntity(dto: CreatePersonalSkillsAndInterestsDto?): PersonalSkillsAndInterestsEntity? =
    dto?.let {
      PersonalSkillsAndInterestsEntity(
        reference = UUID.randomUUID(),
        createdAtPrison = it.prisonId,
        updatedAtPrison = it.prisonId,
      ).apply {
        addNewSkills(it.skills, this)
        addNewInterests(it.interests, this)
      }
    }

  fun fromEntityToDomain(persistedEntity: PersonalSkillsAndInterestsEntity?): PersonalSkillsAndInterests? =
    persistedEntity?.let {
      PersonalSkillsAndInterests(
        reference = it.reference,
        skills = it.skills.map { personalSkillEntityMapper.fromEntityToDomain(it) },
        interests = it.interests.map { personalInterestEntityMapper.fromEntityToDomain(it) },
        createdBy = it.createdBy!!,
        createdAt = it.createdAt!!,
        createdAtPrison = it.createdAtPrison,
        lastUpdatedBy = it.updatedBy!!,
        lastUpdatedAt = it.updatedAt!!,
        lastUpdatedAtPrison = it.updatedAtPrison,
      )
    }

  fun updateExistingEntityFromDto(entity: PersonalSkillsAndInterestsEntity, dto: UpdatePersonalSkillsAndInterestsDto?) =
    dto?.also {
      with(entity) {
        updatedAtPrison = it.prisonId

        val existingSkills = entity.skills
        val updatedSkills = dto.skills
        personalSkillEntityListManager.updateExisting(existingSkills, updatedSkills, personalSkillEntityMapper)
        personalSkillEntityListManager.addNew(entity, existingSkills, updatedSkills, personalSkillEntityMapper)
        personalSkillEntityListManager.deleteRemoved(existingSkills, updatedSkills)

        val existingInterests = entity.interests
        val updatedInterests = dto.interests
        personalInterestEntityListManager.updateExisting(existingInterests, updatedInterests, personalInterestEntityMapper)
        personalInterestEntityListManager.addNew(entity, existingInterests, updatedInterests, personalInterestEntityMapper)
        personalInterestEntityListManager.deleteRemoved(existingInterests, updatedInterests)
      }
    }

  fun fromUpdateDtoToNewEntity(personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsDto?): PersonalSkillsAndInterestsEntity? =
    personalSkillsAndInterests?.let {
      PersonalSkillsAndInterestsEntity(
        reference = UUID.randomUUID(),
        createdAtPrison = it.prisonId,
        updatedAtPrison = it.prisonId,
      ).apply {
        addNewSkills(it.skills, this)
        addNewInterests(it.interests, this)
      }
    }

  private fun addNewSkills(skills: List<PersonalSkill>, entity: PersonalSkillsAndInterestsEntity) {
    skills.forEach {
      entity.addChild(
        personalSkillEntityMapper.fromDomainToEntity(it),
        entity.skills,
      )
    }
  }

  private fun addNewInterests(interests: List<PersonalInterest>, entity: PersonalSkillsAndInterestsEntity) {
    interests.forEach {
      entity.addChild(
        personalInterestEntityMapper.fromDomainToEntity(it),
        entity.interests,
      )
    }
  }
}

@Component
class PersonalSkillEntityMapper : KeyAwareEntityMapper<PersonalSkillEntity, PersonalSkill> {
  override fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity =
    with(domain) {
      PersonalSkillEntity(
        reference = UUID.randomUUID(),
        skillType = toSkillType(skillType),
        skillTypeOther = skillTypeOther,
      )
    }

  fun fromEntityToDomain(persistedEntity: PersonalSkillEntity): PersonalSkill =
    with(persistedEntity) {
      PersonalSkill(
        skillType = toSkillType(skillType),
        skillTypeOther = skillTypeOther,
      )
    }

  override fun updateEntityFromDomain(entity: PersonalSkillEntity, domain: PersonalSkill) =
    with(entity) {
      skillType = toSkillType(domain.skillType)
      skillTypeOther = domain.skillTypeOther
    }

  private fun toSkillType(skillType: SkillTypeEntity): SkillTypeDomain =
    when (skillType) {
      SkillTypeEntity.COMMUNICATION -> SkillTypeDomain.COMMUNICATION
      SkillTypeEntity.POSITIVE_ATTITUDE -> SkillTypeDomain.POSITIVE_ATTITUDE
      SkillTypeEntity.RESILIENCE -> SkillTypeDomain.RESILIENCE
      SkillTypeEntity.SELF_MANAGEMENT -> SkillTypeDomain.SELF_MANAGEMENT
      SkillTypeEntity.TEAMWORK -> SkillTypeDomain.TEAMWORK
      SkillTypeEntity.THINKING_AND_PROBLEM_SOLVING -> SkillTypeDomain.THINKING_AND_PROBLEM_SOLVING
      SkillTypeEntity.WILLINGNESS_TO_LEARN -> SkillTypeDomain.WILLINGNESS_TO_LEARN
      SkillTypeEntity.OTHER -> SkillTypeDomain.OTHER
      SkillTypeEntity.NONE -> SkillTypeDomain.NONE
    }

  private fun toSkillType(skillType: SkillTypeDomain): SkillTypeEntity =
    when (skillType) {
      SkillTypeDomain.COMMUNICATION -> SkillTypeEntity.COMMUNICATION
      SkillTypeDomain.POSITIVE_ATTITUDE -> SkillTypeEntity.POSITIVE_ATTITUDE
      SkillTypeDomain.RESILIENCE -> SkillTypeEntity.RESILIENCE
      SkillTypeDomain.SELF_MANAGEMENT -> SkillTypeEntity.SELF_MANAGEMENT
      SkillTypeDomain.TEAMWORK -> SkillTypeEntity.TEAMWORK
      SkillTypeDomain.THINKING_AND_PROBLEM_SOLVING -> SkillTypeEntity.THINKING_AND_PROBLEM_SOLVING
      SkillTypeDomain.WILLINGNESS_TO_LEARN -> SkillTypeEntity.WILLINGNESS_TO_LEARN
      SkillTypeDomain.OTHER -> SkillTypeEntity.OTHER
      SkillTypeDomain.NONE -> SkillTypeEntity.NONE
    }
}

@Component
class PersonalInterestEntityMapper : KeyAwareEntityMapper<PersonalInterestEntity, PersonalInterest> {
  override fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity =
    with(domain) {
      PersonalInterestEntity(
        reference = UUID.randomUUID(),
        interestType = toInterestType(interestType),
        interestTypeOther = interestTypeOther,
      )
    }

  fun fromEntityToDomain(persistedEntity: PersonalInterestEntity): PersonalInterest =
    with(persistedEntity) {
      PersonalInterest(
        interestType = toInterestType(interestType),
        interestTypeOther = interestTypeOther,
      )
    }

  override fun updateEntityFromDomain(entity: PersonalInterestEntity, domain: PersonalInterest) =
    with(entity) {
      interestType = toInterestType(domain.interestType)
      interestTypeOther = domain.interestTypeOther
    }

  private fun toInterestType(interestType: InterestTypeEntity): InterestTypeDomain =
    when (interestType) {
      InterestTypeEntity.COMMUNITY -> InterestTypeDomain.COMMUNITY
      InterestTypeEntity.CRAFTS -> InterestTypeDomain.CRAFTS
      InterestTypeEntity.CREATIVE -> InterestTypeDomain.CREATIVE
      InterestTypeEntity.DIGITAL -> InterestTypeDomain.DIGITAL
      InterestTypeEntity.KNOWLEDGE_BASED -> InterestTypeDomain.KNOWLEDGE_BASED
      InterestTypeEntity.MUSICAL -> InterestTypeDomain.MUSICAL
      InterestTypeEntity.OUTDOOR -> InterestTypeDomain.OUTDOOR
      InterestTypeEntity.NATURE_AND_ANIMALS -> InterestTypeDomain.NATURE_AND_ANIMALS
      InterestTypeEntity.SOCIAL -> InterestTypeDomain.SOCIAL
      InterestTypeEntity.SOLO_ACTIVITIES -> InterestTypeDomain.SOLO_ACTIVITIES
      InterestTypeEntity.SOLO_SPORTS -> InterestTypeDomain.SOLO_SPORTS
      InterestTypeEntity.TEAM_SPORTS -> InterestTypeDomain.TEAM_SPORTS
      InterestTypeEntity.WELLNESS -> InterestTypeDomain.WELLNESS
      InterestTypeEntity.OTHER -> InterestTypeDomain.OTHER
      InterestTypeEntity.NONE -> InterestTypeDomain.NONE
    }

  private fun toInterestType(interestType: InterestTypeDomain): InterestTypeEntity =
    when (interestType) {
      InterestTypeDomain.COMMUNITY -> InterestTypeEntity.COMMUNITY
      InterestTypeDomain.CRAFTS -> InterestTypeEntity.CRAFTS
      InterestTypeDomain.CREATIVE -> InterestTypeEntity.CREATIVE
      InterestTypeDomain.DIGITAL -> InterestTypeEntity.DIGITAL
      InterestTypeDomain.KNOWLEDGE_BASED -> InterestTypeEntity.KNOWLEDGE_BASED
      InterestTypeDomain.MUSICAL -> InterestTypeEntity.MUSICAL
      InterestTypeDomain.OUTDOOR -> InterestTypeEntity.OUTDOOR
      InterestTypeDomain.NATURE_AND_ANIMALS -> InterestTypeEntity.NATURE_AND_ANIMALS
      InterestTypeDomain.SOCIAL -> InterestTypeEntity.SOCIAL
      InterestTypeDomain.SOLO_ACTIVITIES -> InterestTypeEntity.SOLO_ACTIVITIES
      InterestTypeDomain.SOLO_SPORTS -> InterestTypeEntity.SOLO_SPORTS
      InterestTypeDomain.TEAM_SPORTS -> InterestTypeEntity.TEAM_SPORTS
      InterestTypeDomain.WELLNESS -> InterestTypeEntity.WELLNESS
      InterestTypeDomain.OTHER -> InterestTypeEntity.OTHER
      InterestTypeDomain.NONE -> InterestTypeEntity.NONE
    }
}
