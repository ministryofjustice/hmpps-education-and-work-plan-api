package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Table(name = "timeline_event")
@Entity
class TimelineEventEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var sourceReference: String? = null,

  @Column(updatable = false)
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var eventType: TimelineEventType? = null,

  @Column(updatable = false)
  var contextualInfo: String? = null,

  /**
   * The ID of the prison that the event relates to.
   */
  @Column(updatable = false)
  @field:NotNull
  var createdAtPrison: String? = null,

  /**
   * Username of the person who created the original event (not to be confused with who/what saved this entity)
   */
  @Column(updatable = false)
  @field:NotNull
  var createdBy: String? = null,

  /**
   * Name of the person who created the original event (not to be confused with who/what saved this entity)
   */
  @Column(updatable = false)
  var createdByDisplayName: String? = null,

  /**
   * The timestamp of the original event (not when this entity was saved to the DB - see createdAt).
   */
  @Column(updatable = false)
  @field:NotNull
  var timestamp: Instant? = null,

  /**
   * The timestamp that this entity was created.
   */
  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as TimelineEventEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, sourceReference = $sourceReference, eventType = $eventType)"
  }
}

enum class TimelineEventType {
  ACTION_PLAN_CREATED,
  GOAL_CREATED,
  GOAL_UPDATED,
  GOAL_STARTED,
  GOAL_COMPLETED,
  GOAL_ARCHIVED,
  STEP_UPDATED,
  STEP_NOT_STARTED,
  STEP_STARTED,
  STEP_COMPLETED,
}
