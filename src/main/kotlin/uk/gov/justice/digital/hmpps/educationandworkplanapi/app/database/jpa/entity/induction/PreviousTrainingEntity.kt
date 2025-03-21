package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * Holds details of any additional training that a Prisoner may have done.
 *
 * Note that the list of training cannot be empty, since NONE is an option.
 */
@Table(name = "previous_training")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class PreviousTrainingEntity(
  @Column(updatable = false)
  val reference: UUID,

  @ElementCollection(targetClass = TrainingType::class)
  @Enumerated(value = EnumType.STRING)
  @CollectionTable(name = "training_type", joinColumns = [JoinColumn(name = "training_id")])
  @Column(name = "type")
  var trainingTypes: List<TrainingType>,

  @Column
  var trainingTypeOther: String? = null,

  @Column
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) {
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PreviousTrainingEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, trainingTypes = $trainingTypes, trainingTypeOther = $trainingTypeOther)"
}

enum class TrainingType {
  CSCS_CARD,
  FIRST_AID_CERTIFICATE,
  FOOD_HYGIENE_CERTIFICATE,
  FULL_UK_DRIVING_LICENCE,
  HEALTH_AND_SAFETY,
  HGV_LICENCE,
  MACHINERY_TICKETS,
  MANUAL_HANDLING,
  TRADE_COURSE,
  OTHER,
  NONE,
}
