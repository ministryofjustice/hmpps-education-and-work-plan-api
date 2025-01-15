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
 * Represents any in-prison work or training interests a Prisoner might have during their time in prison.
 */
@Table(name = "in_prison_interests")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class InPrisonInterestsEntity(
  @Column(updatable = false)
  val reference: UUID,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val inPrisonWorkInterests: MutableList<InPrisonWorkInterestEntity> = mutableListOf(),

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestEntity> = mutableListOf(),

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
    other as InPrisonInterestsEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "in_prison_work_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class InPrisonWorkInterestEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var workType: InPrisonWorkType,

  @Column
  var workTypeOther: String? = null,
) : KeyAwareChildEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id")
  var parent: InPrisonInterestsEntity? = null

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
    this.parent = parent as InPrisonInterestsEntity
  }

  override fun key(): String = workType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InPrisonWorkInterestEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, workType = $workType, workTypeOther = $workTypeOther)"
  }
}

@Table(name = "in_prison_training_interest")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class InPrisonTrainingInterestEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var trainingType: InPrisonTrainingType,

  @Column
  var trainingTypeOther: String? = null,
) : KeyAwareChildEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id")
  var parent: InPrisonInterestsEntity? = null

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

  override fun associateWithParent(parent: ParentEntity) {
    this.parent = parent as InPrisonInterestsEntity
  }

  override fun key(): String = trainingType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InPrisonTrainingInterestEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, trainingType = $trainingType, trainingTypeOther = $trainingTypeOther)"
  }
}

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
