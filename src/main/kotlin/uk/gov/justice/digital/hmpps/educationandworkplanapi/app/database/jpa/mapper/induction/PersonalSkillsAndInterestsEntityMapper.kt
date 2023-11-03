package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePersonalSkillsAndInterestsDto

@Mapper(
  uses = [
    PersonalSkillEntityMapper::class,
    PersonalInterestEntityMapper::class,
  ],
)
abstract class PersonalSkillsAndInterestsEntityMapper {

  @Autowired
  private lateinit var personalSkillEntityMapper: PersonalSkillEntityMapper

  @Autowired
  private lateinit var personalInterestEntityMapper: PersonalInterestEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreatePersonalSkillsAndInterestsDto): PersonalSkillsAndInterestsEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: PersonalSkillsAndInterestsEntity): PersonalSkillsAndInterests

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "skills", expression = "java( updateSkills(entity, dto) )")
  @Mapping(target = "interests", expression = "java( updateInterests(entity, dto) )")
  abstract fun updateEntityFromDto(
    @MappingTarget entity: PersonalSkillsAndInterestsEntity?,
    dto: UpdatePersonalSkillsAndInterestsDto?,
  )

  fun updateSkills(
    entity: PersonalSkillsAndInterestsEntity,
    dto: UpdatePersonalSkillsAndInterestsDto,
  ): List<PersonalSkillEntity> {
    val existingSkills = entity.skills!!
    val updatedSkills = dto.skills

    updateExistingSkills(existingSkills, updatedSkills)
    addNewSkills(existingSkills, updatedSkills)
    removeSkills(existingSkills, updatedSkills)

    return existingSkills
  }

  fun updateInterests(
    entity: PersonalSkillsAndInterestsEntity,
    dto: UpdatePersonalSkillsAndInterestsDto,
  ): List<PersonalInterestEntity> {
    val existingInterests = entity.interests!!
    val updatedInterests = dto.interests

    updateExistingInterests(existingInterests, updatedInterests)
    addNewInterests(existingInterests, updatedInterests)
    removeInterests(existingInterests, updatedInterests)

    return existingInterests
  }

  private fun updateExistingSkills(
    existingSkills: MutableList<PersonalSkillEntity>,
    updatedSkills: List<PersonalSkill>,
  ) {
    val updatedSkillTypes = updatedSkills.map { it.skillType.name }
    existingSkills
      .filter { skillEntity -> updatedSkillTypes.contains(skillEntity.skillType!!.name) }
      .onEach { skillEntity ->
        personalSkillEntityMapper.updateEntityFromDomain(
          skillEntity,
          updatedSkills.first { updatedInterestDomain -> updatedInterestDomain.skillType.name == skillEntity.skillType!!.name },
        )
      }
  }

  private fun addNewSkills(
    existingSkills: MutableList<PersonalSkillEntity>,
    updatedSkills: List<PersonalSkill>,
  ) {
    val currentInterestTypes = existingSkills.map { it.skillType!!.name }
    existingSkills.addAll(
      updatedSkills
        .filter { updatedInterestDto -> !currentInterestTypes.contains(updatedInterestDto.skillType.name) }
        .map { newInterestDto -> personalSkillEntityMapper.fromDomainToEntity(newInterestDto) },
    )
  }

  private fun removeSkills(
    existingSkills: MutableList<PersonalSkillEntity>,
    updatedSkills: List<PersonalSkill>,
  ) {
    val updatedInterestTypes = updatedSkills.map { it.skillType.name }
    existingSkills.removeIf { skillEntity ->
      !updatedInterestTypes.contains(skillEntity.skillType!!.name)
    }
  }

  private fun updateExistingInterests(
    existingInterests: MutableList<PersonalInterestEntity>,
    updatedInterests: List<PersonalInterest>,
  ) {
    val updatedInterestTypes = updatedInterests.map { it.interestType.name }
    existingInterests
      .filter { interestEntity -> updatedInterestTypes.contains(interestEntity.interestType!!.name) }
      .onEach { interestEntity ->
        personalInterestEntityMapper.updateEntityFromDomain(
          interestEntity,
          updatedInterests.first { updatedInterestDomain -> updatedInterestDomain.interestType.name == interestEntity.interestType!!.name },
        )
      }
  }

  private fun addNewInterests(
    existingInterests: MutableList<PersonalInterestEntity>,
    updatedInterests: List<PersonalInterest>,
  ) {
    val currentInterestTypes = existingInterests.map { it.interestType!!.name }
    existingInterests.addAll(
      updatedInterests
        .filter { updatedInterestDto -> !currentInterestTypes.contains(updatedInterestDto.interestType.name) }
        .map { newInterestDto -> personalInterestEntityMapper.fromDomainToEntity(newInterestDto) },
    )
  }

  private fun removeInterests(
    existingInterests: MutableList<PersonalInterestEntity>,
    updatedInterests: List<PersonalInterest>,
  ) {
    val updatedInterestTypes = updatedInterests.map { it.interestType.name }
    existingInterests.removeIf { interestEntity ->
      !updatedInterestTypes.contains(interestEntity.interestType!!.name)
    }
  }
}

@Mapper
interface PersonalSkillEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity

  fun fromEntityToDomain(persistedEntity: PersonalSkillEntity): PersonalSkill

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: PersonalSkillEntity, domain: PersonalSkill)
}

@Mapper
interface PersonalInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity

  fun fromEntityToDomain(persistedEntity: PersonalInterestEntity): PersonalInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: PersonalInterestEntity, domain: PersonalInterest)
}
