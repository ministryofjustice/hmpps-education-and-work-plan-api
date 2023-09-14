package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Table(name = "timeline")
@Entity
class TimelineEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var prisonNumber: String,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "timeline_id", nullable = false)
  @OrderBy(value = "createdAt")
  @field:NotNull
  var events: MutableList<TimelineEventEntity>? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,
) {

  companion object {

    /**
     * Returns new un-persisted [TimelineEntity] for the specified prisoner with an empty collection of
     * [TimelineEventEntity]s.
     */
    fun newTimelineForPrisoner(prisonNumber: String): TimelineEntity =
      TimelineEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        events = mutableListOf(),
      )
  }

  fun addEvent(timelineEvent: TimelineEventEntity): TimelineEntity {
    events!!.add(timelineEvent)
    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as TimelineEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, prisonNumber = $prisonNumber)"
  }
}
