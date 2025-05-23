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
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
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
@EntityListeners(value = [AuditingEntityListener::class])
data class PersonalSkillsAndInterestsEntity(
  @Column(updatable = false)
  val reference: UUID,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val skills: MutableList<PersonalSkillEntity> = mutableListOf(),

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val interests: MutableList<PersonalInterestEntity> = mutableListOf(),

  @Column
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) : ParentEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

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

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference)"
}

@Table(name = "personal_skill")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class PersonalSkillEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var skillType: SkillType,

  @Column
  var skillTypeOther: String? = null,
) : KeyAwareChildEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsEntity? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

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

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, skillType = $skillType, skillTypeOther = $skillTypeOther)"
}

@Table(name = "personal_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class PersonalInterestEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var interestType: InterestType,

  @Column
  var interestTypeOther: String? = null,
) : KeyAwareChildEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id")
  var parent: PersonalSkillsAndInterestsEntity? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

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

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, interestType = $interestType, interestTypeOther = $interestTypeOther)"
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
