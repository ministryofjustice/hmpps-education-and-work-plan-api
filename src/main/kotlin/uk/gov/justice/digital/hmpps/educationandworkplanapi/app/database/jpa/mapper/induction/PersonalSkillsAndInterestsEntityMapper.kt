package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.domain.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    PersonalSkillEntityMapper::class,
    PersonalInterestEntityMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PersonalSkillsAndInterestsEntityMapper {

  @Autowired
  private lateinit var personalSkillEntityMapper: PersonalSkillEntityMapper

  @Autowired
  private lateinit var personalInterestEntityMapper: PersonalInterestEntityMapper

  @Autowired
  private lateinit var personalSkillEntityListManager: InductionEntityListManager<PersonalSkillEntity, PersonalSkill>

  @Autowired
  private lateinit var personalInterestEntityListManager: InductionEntityListManager<PersonalInterestEntity, PersonalInterest>

  @BeanMapping(qualifiedByName = ["addNewSkillsAndInterestsDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "skills", ignore = true)
  @Mapping(target = "interests", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreatePersonalSkillsAndInterestsDto): PersonalSkillsAndInterestsEntity

  @Named("addNewSkillsAndInterestsDuringCreate")
  @AfterMapping
  fun addNewSkillsAndInterestsDuringCreate(
    dto: CreatePersonalSkillsAndInterestsDto,
    @MappingTarget entity: PersonalSkillsAndInterestsEntity,
  ) {
    addNewSkills(dto.skills, entity)
    addNewInterests(dto.interests, entity)
  }

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
  abstract fun updateExistingEntityFromDto(
    @MappingTarget entity: PersonalSkillsAndInterestsEntity,
    dto: UpdatePersonalSkillsAndInterestsDto?,
  )

  fun updateSkills(
    entity: PersonalSkillsAndInterestsEntity,
    dto: UpdatePersonalSkillsAndInterestsDto,
  ): List<PersonalSkillEntity> {
    val existingSkills = entity.skills!!
    val updatedSkills = dto.skills

    personalSkillEntityListManager.updateExisting(existingSkills, updatedSkills, personalSkillEntityMapper)
    personalSkillEntityListManager.addNew(entity, existingSkills, updatedSkills, personalSkillEntityMapper)
    personalSkillEntityListManager.deleteRemoved(existingSkills, updatedSkills)

    return existingSkills
  }

  fun updateInterests(
    entity: PersonalSkillsAndInterestsEntity,
    dto: UpdatePersonalSkillsAndInterestsDto,
  ): List<PersonalInterestEntity> {
    val existingInterests = entity.interests!!
    val updatedInterests = dto.interests

    personalInterestEntityListManager.updateExisting(existingInterests, updatedInterests, personalInterestEntityMapper)
    personalInterestEntityListManager.addNew(entity, existingInterests, updatedInterests, personalInterestEntityMapper)
    personalInterestEntityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
  }

  @BeanMapping(qualifiedByName = ["addNewSkillsAndInterestsDuringUpdate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "skills", ignore = true)
  @Mapping(target = "interests", ignore = true)
  abstract fun fromUpdateDtoToNewEntity(personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsDto?): PersonalSkillsAndInterestsEntity?

  @Named("addNewSkillsAndInterestsDuringUpdate")
  @AfterMapping
  fun addNewSkillsAndInterestsDuringUpdate(
    dto: UpdatePersonalSkillsAndInterestsDto,
    @MappingTarget entity: PersonalSkillsAndInterestsEntity,
  ) {
    addNewSkills(dto.skills, entity)
    addNewInterests(dto.interests, entity)
  }

  private fun addNewSkills(
    skills: List<PersonalSkill>,
    entity: PersonalSkillsAndInterestsEntity,
  ) {
    skills.forEach {
      entity.addChild(
        personalSkillEntityMapper.fromDomainToEntity(it),
        entity.skills(),
      )
    }
  }

  private fun addNewInterests(
    interests: List<PersonalInterest>,
    entity: PersonalSkillsAndInterestsEntity,
  ) {
    interests.forEach {
      entity.addChild(
        personalInterestEntityMapper.fromDomainToEntity(it),
        entity.interests(),
      )
    }
  }
}

@Mapper
interface PersonalSkillEntityMapper :
  KeyAwareEntityMapper<PersonalSkillEntity, PersonalSkill> {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity

  fun fromEntityToDomain(persistedEntity: PersonalSkillEntity): PersonalSkill

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: PersonalSkillEntity, domain: PersonalSkill)
}

@Mapper
interface PersonalInterestEntityMapper :
  KeyAwareEntityMapper<PersonalInterestEntity, PersonalInterest> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity

  fun fromEntityToDomain(persistedEntity: PersonalInterestEntity): PersonalInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: PersonalInterestEntity, domain: PersonalInterest)
}
