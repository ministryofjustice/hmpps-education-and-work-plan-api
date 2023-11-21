package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Lists the personal skills (such as communication) and interests (such as music) that a Prisoner feels they have.
 *
 * Note that the lists of skills/interests cannot be empty, since NONE is an option in both cases.
 */
@Table(name = "skills_and_interests")
@Entity
class PersonalSkillsAndInterestsMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var skills: MutableList<PersonalSkillMigrationEntity>? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var interests: MutableList<PersonalInterestMigrationEntity>? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var createdByDisplayName: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  var updatedBy: String? = null,

  @Column
  var updatedByDisplayName: String? = null,
) : ParentMigrationEntity() {

  fun skills(): MutableList<PersonalSkillMigrationEntity> {
    if (skills == null) {
      skills = mutableListOf()
    }
    return skills!!
  }

  fun interests(): MutableList<PersonalInterestMigrationEntity> {
    if (interests == null) {
      interests = mutableListOf()
    }
    return interests!!
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalSkillsAndInterestsMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "personal_skill")
@Entity
class PersonalSkillMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var skillType: SkillType? = null,

  @Column
  var skillTypeOther: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column
  var updatedBy: String? = null,
) : KeyAwareChildMigrationEntity {

  override fun associateWithParent(parent: ParentMigrationEntity) {
    this.parent = parent as PersonalSkillsAndInterestsMigrationEntity
  }

  override fun key(): String = skillType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalSkillMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, skillType = $skillType, skillTypeOther = $skillTypeOther)"
  }
}

@Table(name = "personal_interest")
@Entity
class PersonalInterestMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var interestType: InterestType? = null,

  @Column
  var interestTypeOther: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column
  var updatedBy: String? = null,
) : KeyAwareChildMigrationEntity {

  override fun associateWithParent(parent: ParentMigrationEntity) {
    this.parent = parent as PersonalSkillsAndInterestsMigrationEntity
  }

  override fun key(): String = interestType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalInterestMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, interestType = $interestType, interestTypeOther = $interestTypeOther)"
  }
}

enum class SkillType {
  COMMUNICATION,
  POSITIVE_ATTITUDE,
  RESILIENCE,
  SELF_MANAGEMENT,
  TEAMWORK,
  THINKING_AND_PROBLEM_SOLVING,
  WILLINGNESS_TO_LEARN,
  OTHER,
  NONE,
}

enum class InterestType {
  COMMUNITY,
  CRAFTS,
  CREATIVE,
  DIGITAL,
  KNOWLEDGE_BASED,
  MUSICAL,
  OUTDOOR,
  NATURE_AND_ANIMALS,
  SOCIAL,
  SOLO_ACTIVITIES,
  SOLO_SPORTS,
  TEAM_SPORTS,
  WELLNESS,
  OTHER,
  NONE,
}
