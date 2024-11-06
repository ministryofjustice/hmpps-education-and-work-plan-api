package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
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

@Table(name = "note")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class NoteEntity(
  @Column(updatable = false)
  val prisonNumber: String,

  @Column(updatable = false)
  val reference: UUID,

  @Column
  var content: String,

  @Column(updatable = false)
  @Enumerated(value = EnumType.STRING)
  val noteType: NoteType,

  @Column(updatable = false)
  @Enumerated(value = EnumType.STRING)
  val entityType: EntityType,

  @Column(updatable = false)
  val entityReference: UUID,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

  @Column(updatable = false)
  @LastModifiedBy
  var updatedBy: String? = null

  @Column(updatable = false)
  @UpdateTimestamp
  var updatedAt: Instant? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as NoteEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

enum class EntityType {
  GOAL,
  REVIEW,
  INDUCTION,
}

enum class NoteType(val entityType: EntityType) {
  GOAL(EntityType.GOAL),
  GOAL_ARCHIVAL(EntityType.GOAL),
  GOAL_COMPLETION(EntityType.GOAL),
  REVIEW(EntityType.REVIEW),
  INDUCTION(EntityType.INDUCTION),
}
