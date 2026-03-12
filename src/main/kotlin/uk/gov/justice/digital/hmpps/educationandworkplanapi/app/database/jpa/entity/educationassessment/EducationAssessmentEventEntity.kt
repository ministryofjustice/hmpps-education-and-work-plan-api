package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment

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
import java.time.LocalDate
import java.util.UUID

/**
 * Represents an event from an external system that notifies a prisoner's Education Assessment status has changed.
 *
 * This data is not part of the core LWP domain. It is not considered part of a prisoners Learning and Work Plan,
 * but we do need to store it as some LWP processes are triggered by a prisoner having completed all of their
 * Education Assessments in the external system.
 *
 * The `source` field is used to record the source of the event. At time of writing Education Assessments are only
 * conducted in Curious but this may change at some point.
 */
@Table(name = "education_assessment_event")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class EducationAssessmentEventEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  val status: EducationAssessmentEventStatus,

  @Column(updatable = false)
  val statusChangeDate: LocalDate,

  @Column(updatable = false)
  val source: String,

  @Column(updatable = false)
  val detailUrl: String?,

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
    other as EducationAssessmentEventEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id=$id, prisonNumber=$prisonNumber, status=$status, source=$source)"
}

enum class EducationAssessmentEventStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
