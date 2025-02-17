package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.MapKeyEnumerated
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Table(name = "timeline_event")
@Entity
data class TimelineEventEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val sourceReference: String,

  @Column(updatable = false)
  @Enumerated(value = EnumType.STRING)
  val eventType: TimelineEventType,

  @ElementCollection
  @MapKeyColumn(name = "name")
  @MapKeyEnumerated(EnumType.STRING)
  @Column(name = "value")
  @CollectionTable(
    name = "timeline_event_contextual_info",
    joinColumns = [JoinColumn(name = "timeline_event_id")],
  )
  val contextualInfo: Map<TimelineEventContext, String>,

  /**
   * The ID of the prison that the prisoner was at when the event occurred.
   */
  @Column(updatable = false)
  val prisonId: String,

  /**
   * The username of the person who caused this event. Set to 'system' if the event was not actioned by a DPS user.
   */
  @Column(updatable = false)
  val actionedBy: String,

  /**
   * The timestamp of the original event (not when this entity was saved to the DB - see createdAt).
   */
  @Column(updatable = false)
  val timestamp: Instant,

  /**
   * A correlation ID.
   */
  @Column(updatable = false)
  val correlationId: UUID,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

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
  INDUCTION_CREATED,
  INDUCTION_UPDATED,
  INDUCTION_SCHEDULE_CREATED,
  INDUCTION_SCHEDULE_UPDATED,
  INDUCTION_SCHEDULE_STATUS_UPDATED,
  ACTION_PLAN_CREATED,
  GOAL_CREATED,
  GOAL_UPDATED,
  GOAL_COMPLETED,
  GOAL_ARCHIVED,
  GOAL_UNARCHIVED,
  STEP_UPDATED,
  STEP_NOT_STARTED,
  STEP_STARTED,
  STEP_COMPLETED,
  ACTION_PLAN_REVIEW_COMPLETED,
  ACTION_PLAN_REVIEW_SCHEDULE_CREATED,
  ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED,
  PRISON_ADMISSION,
  PRISON_RELEASE,
  PRISON_TRANSFER,
}

enum class TimelineEventContext {
  GOAL_TITLE,
  STEP_TITLE,
  GOAL_ARCHIVED_REASON,
  GOAL_ARCHIVED_REASON_OTHER,
  PRISON_TRANSFERRED_FROM,
  INDUCTION_SCHEDULE_STATUS,
  INDUCTION_SCHEDULE_DEADLINE_DATE,
  COMPLETED_REVIEW_ENTERED_ONLINE_AT,
  COMPLETED_REVIEW_ENTERED_ONLINE_BY,
  COMPLETED_REVIEW_NOTES,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE,
  COMPLETED_INDUCTION_ENTERED_ONLINE_AT,
  COMPLETED_INDUCTION_ENTERED_ONLINE_BY,
  COMPLETED_INDUCTION_NOTES,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE,
  REVIEW_SCHEDULE_STATUS_OLD,
  REVIEW_SCHEDULE_STATUS_NEW,
  REVIEW_SCHEDULE_DEADLINE_OLD,
  REVIEW_SCHEDULE_DEADLINE_NEW,
  REVIEW_SCHEDULE_EXEMPTION_REASON,
  INDUCTION_SCHEDULE_STATUS_OLD,
  INDUCTION_SCHEDULE_STATUS_NEW,
  INDUCTION_SCHEDULE_DEADLINE_OLD,
  INDUCTION_SCHEDULE_DEADLINE_NEW,
  INDUCTION_SCHEDULE_EXEMPTION_REASON,
}
