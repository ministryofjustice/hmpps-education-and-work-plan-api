package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table(name = "review_schedule_history")
@Entity
data class ReviewScheduleHistoryEntity(

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  val version: Int,

  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @Column
  var earliestReviewDate: LocalDate,

  @Column
  var latestReviewDate: LocalDate,

  @Column
  @Enumerated(value = EnumType.STRING)
  var scheduleCalculationRule: ReviewScheduleCalculationRule,

  @Column
  @Enumerated(value = EnumType.STRING)
  var scheduleStatus: ReviewScheduleStatus,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column
  var updatedBy: String? = null,

  @Column
  var updatedAt: Instant? = null,
)
