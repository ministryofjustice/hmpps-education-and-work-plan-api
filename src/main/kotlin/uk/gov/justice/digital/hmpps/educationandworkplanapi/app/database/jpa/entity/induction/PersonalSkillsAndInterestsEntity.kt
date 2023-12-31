package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.CreatedByDisplayName
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.LastModifiedByDisplayName
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.KeyAwareChildEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ParentEntity
import java.time.Instant
import java.util.UUID

/**
 * Lists the personal skills (such as communication) and interests (such as music) that a Prisoner feels they have.
 *
 * Note that the lists of skills/interests cannot be empty, since NONE is an option in both cases.
 */
@Table(name = "skills_and_interests")
@Entity
@EntityListeners(value = [AuditingEntityListener::class, DisplayNameAuditingEntityListener::class])
class PersonalSkillsAndInterestsEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var skills: MutableList<PersonalSkillEntity>? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var interests: MutableList<PersonalInterestEntity>? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @CreatedByDisplayName
  var createdByDisplayName: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,

  @Column
  @LastModifiedByDisplayName
  var updatedByDisplayName: String? = null,
) : ParentEntity {

  fun skills(): MutableList<PersonalSkillEntity> {
    if (skills == null) {
      skills = mutableListOf()
    }
    return skills!!
  }

  fun interests(): MutableList<PersonalInterestEntity> {
    if (interests == null) {
      interests = mutableListOf()
    }
    return interests!!
  }

  override fun childEntityUpdated() {
    this.updatedAt = Instant.now()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalSkillsAndInterestsEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "personal_skill")
@Entity
@EntityListeners(AuditingEntityListener::class)
class PersonalSkillEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var skillType: SkillType? = null,

  @Column
  var skillTypeOther: String? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
) : KeyAwareChildEntity {

  @PrePersist
  @PreUpdate
  @PreRemove
  fun onChange() {
    parent?.childEntityUpdated()
  }

  override fun associateWithParent(parent: ParentEntity) {
    this.parent = parent as PersonalSkillsAndInterestsEntity
  }

  override fun key(): String = skillType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalSkillEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, skillType = $skillType, skillTypeOther = $skillTypeOther)"
  }
}

@Table(name = "personal_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
class PersonalInterestEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var interestType: InterestType? = null,

  @Column
  var interestTypeOther: String? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
) : KeyAwareChildEntity {

  @PrePersist
  @PreUpdate
  @PreRemove
  fun onChange() {
    parent?.childEntityUpdated()
  }

  override fun associateWithParent(parent: ParentEntity) {
    this.parent = parent as PersonalSkillsAndInterestsEntity
  }

  override fun key(): String = interestType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PersonalInterestEntity

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
