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
) {
  fun fromCreateDtoToEntity(dto: CreatePersonalSkillsAndInterestsDto): PersonalSkillsAndInterestsEntity = with(dto) {
    PersonalSkillsAndInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewSkills(dto.skills, this)
      addNewInterests(dto.interests, this)
    }
  }

  fun fromEntityToDomain(persistedEntity: PersonalSkillsAndInterestsEntity?): PersonalSkillsAndInterests? = persistedEntity?.let {
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

  fun updateExistingEntityFromDto(entity: PersonalSkillsAndInterestsEntity, dto: UpdatePersonalSkillsAndInterestsDto?) = dto?.also {
    with(entity) {
      updatedAtPrison = it.prisonId

      val existingSkills = entity.skills
      val updatedSkills = dto.skills
      updateExistingPersonalSkills(existingSkills, updatedSkills)
      addNewPersonalSkills(entity, existingSkills, updatedSkills)
      deleteRemovedPersonalSkills(existingSkills, updatedSkills)

      val existingInterests = entity.interests
      val updatedInterests = dto.interests
      updateExistingPersonalInterests(existingInterests, updatedInterests)
      addNewPersonalInterests(entity, existingInterests, updatedInterests)
      deleteRemovedPersonalInterests(existingInterests, updatedInterests)
    }
  }

  fun fromUpdateDtoToNewEntity(dto: UpdatePersonalSkillsAndInterestsDto): PersonalSkillsAndInterestsEntity = with(dto) {
    PersonalSkillsAndInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewSkills(dto.skills, this)
      addNewInterests(dto.interests, this)
    }
  }

  private fun addNewSkills(skills: List<PersonalSkill>, entity: PersonalSkillsAndInterestsEntity) {
    entity.addSkillsChildren(skills.map { personalSkillEntityMapper.fromDomainToEntity(it) })
  }

  private fun addNewInterests(interests: List<PersonalInterest>, entity: PersonalSkillsAndInterestsEntity) {
    entity.addInterestsChildren(interests.map { personalInterestEntityMapper.fromDomainToEntity(it) })
  }

  private fun updateExistingPersonalSkills(
    existingEntities: MutableList<PersonalSkillEntity>,
    updatedDomain: List<PersonalSkill>,
  ) {
    val updatedDomainKeys = updatedDomain.map { it.skillType.name }
    existingEntities
      .filter { entity -> updatedDomainKeys.contains(entity.skillType.name) }
      .onEach { entity ->
        personalSkillEntityMapper.updateEntityFromDomain(
          entity,
          updatedDomain.first { dto -> dto.skillType.name == entity.skillType.name },
        )
      }
  }

  private fun addNewPersonalSkills(
    inPrisonInterestsEntity: PersonalSkillsAndInterestsEntity,
    existingEntities: MutableList<PersonalSkillEntity>,
    updatedDomain: List<PersonalSkill>,
  ) {
    val currentIdentifiers = existingEntities.map { it.skillType.name }

    val newEntities = updatedDomain
      .filter { dto -> !currentIdentifiers.contains(dto.skillType.name) }
      .map { newDto -> personalSkillEntityMapper.fromDomainToEntity(newDto) }

    inPrisonInterestsEntity.addSkillsChildren(newEntities)
  }

  private fun deleteRemovedPersonalSkills(
    existingEntities: MutableList<PersonalSkillEntity>,
    updatedDomain: List<PersonalSkill>,
  ) {
    val updatedIdentifiers = updatedDomain.map { it.skillType.name }

    val removedEntities = existingEntities.filter { entity -> !updatedIdentifiers.contains(entity.skillType.name) }
    if (removedEntities.isNotEmpty()) {
      existingEntities.removeAll(removedEntities)
    }
  }

  private fun updateExistingPersonalInterests(
    existingEntities: MutableList<PersonalInterestEntity>,
    updatedDomain: List<PersonalInterest>,
  ) {
    val updatedDomainKeys = updatedDomain.map { it.interestType.name }
    existingEntities
      .filter { entity -> updatedDomainKeys.contains(entity.interestType.name) }
      .onEach { entity ->
        personalInterestEntityMapper.updateEntityFromDomain(
          entity,
          updatedDomain.first { dto -> dto.interestType.name == entity.interestType.name },
        )
      }
  }

  private fun addNewPersonalInterests(
    inPrisonInterestsEntity: PersonalSkillsAndInterestsEntity,
    existingEntities: MutableList<PersonalInterestEntity>,
    updatedDomain: List<PersonalInterest>,
  ) {
    val currentIdentifiers = existingEntities.map { it.interestType.name }

    val newEntities = updatedDomain
      .filter { dto -> !currentIdentifiers.contains(dto.interestType.name) }
      .map { newDto -> personalInterestEntityMapper.fromDomainToEntity(newDto) }

    inPrisonInterestsEntity.addInterestsChildren(newEntities)
  }

  private fun deleteRemovedPersonalInterests(
    existingEntities: MutableList<PersonalInterestEntity>,
    updatedDomain: List<PersonalInterest>,
  ) {
    val updatedIdentifiers = updatedDomain.map { it.interestType.name }

    val removedEntities = existingEntities.filter { entity -> !updatedIdentifiers.contains(entity.interestType.name) }
    if (removedEntities.isNotEmpty()) {
      existingEntities.removeAll(removedEntities)
    }
  }
}

@Component
class PersonalSkillEntityMapper {
  fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity = with(domain) {
    PersonalSkillEntity(
      reference = UUID.randomUUID(),
      skillType = toSkillType(skillType),
      skillTypeOther = skillTypeOther,
    )
  }

  fun fromEntityToDomain(persistedEntity: PersonalSkillEntity): PersonalSkill = with(persistedEntity) {
    PersonalSkill(
      skillType = toSkillType(skillType),
      skillTypeOther = skillTypeOther,
    )
  }

  fun updateEntityFromDomain(entity: PersonalSkillEntity, domain: PersonalSkill) = with(entity) {
    skillType = toSkillType(domain.skillType)
    skillTypeOther = domain.skillTypeOther
  }

  private fun toSkillType(skillType: SkillTypeEntity): SkillTypeDomain = when (skillType) {
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

  private fun toSkillType(skillType: SkillTypeDomain): SkillTypeEntity = when (skillType) {
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
class PersonalInterestEntityMapper {
  fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity = with(domain) {
    PersonalInterestEntity(
      reference = UUID.randomUUID(),
      interestType = toInterestType(interestType),
      interestTypeOther = interestTypeOther,
    )
  }

  fun fromEntityToDomain(persistedEntity: PersonalInterestEntity): PersonalInterest = with(persistedEntity) {
    PersonalInterest(
      interestType = toInterestType(interestType),
      interestTypeOther = interestTypeOther,
    )
  }

  fun updateEntityFromDomain(entity: PersonalInterestEntity, domain: PersonalInterest) = with(entity) {
    interestType = toInterestType(domain.interestType)
    interestTypeOther = domain.interestTypeOther
  }

  private fun toInterestType(interestType: InterestTypeEntity): InterestTypeDomain = when (interestType) {
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

  private fun toInterestType(interestType: InterestTypeDomain): InterestTypeEntity = when (interestType) {
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
