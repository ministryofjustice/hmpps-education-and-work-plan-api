package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
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
class NoteEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column
  var prisonNumber: String? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @field:NotNull
  var content: String? = null,

  @Column
  @field:NotNull
  @Enumerated(value = EnumType.STRING)
  var noteType: NoteType? = null,

  @Column
  @field:NotNull
  @Enumerated(value = EnumType.STRING)
  var entityType: EntityType? = null,

  @Column
  var entityReference: UUID? = null,

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
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
) {
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
}

enum class NoteType(val entityType: EntityType) {
  GOAL(EntityType.GOAL),
  GOAL_ARCHIVAL(EntityType.GOAL),
  GOAL_COMPLETION(EntityType.GOAL),
}
