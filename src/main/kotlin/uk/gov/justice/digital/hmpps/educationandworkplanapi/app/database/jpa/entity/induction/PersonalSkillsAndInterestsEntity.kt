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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * Lists the personal skills (such as communication) and interests (such as music) that a Prisoner feels they have.
 *
 * Note that the lists of skills/interests cannot be empty, since NONE is an option in both cases.
 */
@Table(name = "skills_and_interests")
@Entity
@EntityListeners(AuditingEntityListener::class)
class PersonalSkillsAndInterestsEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id", nullable = false)
  var skills: List<PersonalSkillEntity>? = null,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_interests_id", nullable = false)
  var interests: List<PersonalInterestEntity>? = null,

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
)

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
)

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
)

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
