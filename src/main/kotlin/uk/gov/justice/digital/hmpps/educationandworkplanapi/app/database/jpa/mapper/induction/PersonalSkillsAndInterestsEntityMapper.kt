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

  @Autowired
  private lateinit var personalSkillEntityListManager: InductionEntityListManager<PersonalSkillEntity, PersonalSkill>

  @Autowired
  private lateinit var personalInterestEntityListManager: InductionEntityListManager<PersonalInterestEntity, PersonalInterest>

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

    personalSkillEntityListManager.updateExisting(existingSkills, updatedSkills, personalSkillEntityMapper)
    personalSkillEntityListManager.addNew(existingSkills, updatedSkills, personalSkillEntityMapper)
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
    personalInterestEntityListManager.addNew(existingInterests, updatedInterests, personalInterestEntityMapper)
    personalInterestEntityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
  }
}

@Mapper
interface PersonalSkillEntityMapper :
  KeyAwareEntityMapper<PersonalSkillEntity, PersonalSkill> {

  @ExcludeJpaManagedFields
  @GenerateNewReference
  override fun fromDomainToEntity(domain: PersonalSkill): PersonalSkillEntity

  fun fromEntityToDomain(persistedEntity: PersonalSkillEntity): PersonalSkill

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  override fun updateEntityFromDomain(@MappingTarget entity: PersonalSkillEntity, domain: PersonalSkill)
}

@Mapper
interface PersonalInterestEntityMapper :
  KeyAwareEntityMapper<PersonalInterestEntity, PersonalInterest> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  override fun fromDomainToEntity(domain: PersonalInterest): PersonalInterestEntity

  fun fromEntityToDomain(persistedEntity: PersonalInterestEntity): PersonalInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  override fun updateEntityFromDomain(@MappingTarget entity: PersonalInterestEntity, domain: PersonalInterest)
}
