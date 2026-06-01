package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Table(name = "timeline")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class TimelineEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "timeline_id", nullable = false)
  @OrderBy(value = "createdAt")
  val events: MutableList<TimelineEventEntity>,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreatedDate
  var createdAt: Instant? = null

  @Column
  @LastModifiedDate
  var updatedAt: Instant? = null

  companion object {

    /**
     * Returns new un-persisted [TimelineEntity] for the specified prisoner with an empty collection of
     * [TimelineEventEntity]s.
     */
    fun newTimelineForPrisoner(prisonNumber: String): TimelineEntity = TimelineEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      events = mutableListOf(),
    )
  }

  fun addEvent(timelineEvent: TimelineEventEntity): TimelineEntity {
    events.add(timelineEvent)
    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as TimelineEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, prisonNumber = $prisonNumber)"
}
