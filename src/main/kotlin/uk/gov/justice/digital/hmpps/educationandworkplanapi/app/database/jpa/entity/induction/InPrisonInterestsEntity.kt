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
 * Represents any in-prison work or training interests a Prisoner might have during their time in prison.
 */
@Table(name = "in_prison_interests")
@Entity
@EntityListeners(AuditingEntityListener::class)
class InPrisonInterestsEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id", nullable = false)
  var inPrisonWorkInterests: List<InPrisonWorkInterest>? = null,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id", nullable = false)
  var inPrisonTrainingInterests: List<InPrisonTrainingInterest>? = null,

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

@Table(name = "in_prison_work_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
class InPrisonWorkInterest(
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
  var workType: InPrisonWorkType? = null,

  @Column
  var workTypeOther: String? = null,

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

@Table(name = "in_prison_training_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
class InPrisonTrainingInterest(
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
  var trainingType: InPrisonTrainingType? = null,

  @Column
  var trainingTypeOther: String? = null,

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

enum class InPrisonWorkType {
  CLEANING_AND_HYGIENE,
  COMPUTERS_OR_DESK_BASED,
  GARDENING_AND_OUTDOORS,
  KITCHENS_AND_COOKING,
  MAINTENANCE,
  PRISON_LAUNDRY,
  PRISON_LIBRARY,
  TEXTILES_AND_SEWING,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}

enum class InPrisonTrainingType {
  BARBERING_AND_HAIRDRESSING,
  CATERING,
  COMMUNICATION_SKILLS,
  ENGLISH_LANGUAGE_SKILLS,
  FORKLIFT_DRIVING,
  INTERVIEW_SKILLS,
  MACHINERY_TICKETS,
  NUMERACY_SKILLS,
  RUNNING_A_BUSINESS,
  SOCIAL_AND_LIFE_SKILLS,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}
