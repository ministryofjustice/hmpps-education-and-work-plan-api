package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

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

/**
 * Represents the schedule for when a prisoner's Induction must be completed by.
 */
@Table(name = "induction_schedule_history")
@Entity
data class InductionScheduleHistoryEntity(

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @Column(updatable = false)
  @Enumerated(value = EnumType.STRING)
  val scheduleCalculationRule: InductionScheduleCalculationRule,

  @Column(updatable = true)
  var deadlineDate: LocalDate,

  @Column(updatable = true)
  @Enumerated(value = EnumType.STRING)
  var scheduleStatus: InductionScheduleStatus,

  val version: Int,

  @Column
  var exemptionReason: String?,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column
  var updatedBy: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
)
