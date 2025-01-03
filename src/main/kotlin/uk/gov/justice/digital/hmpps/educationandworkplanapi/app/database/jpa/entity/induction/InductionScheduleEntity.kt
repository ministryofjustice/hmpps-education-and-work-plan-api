package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

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
 * Represents the schedule for when a prisoner's Induction must be completed by.
 */
@Table(name = "induction_schedule")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class InductionScheduleEntity(
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

  @Column
  var exemptionReason: String? = null,
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
    other as InductionScheduleEntity

    return id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}

enum class InductionScheduleCalculationRule {
  NEW_PRISON_ADMISSION,
  EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE,
  EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE,
  EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  EXISTING_PRISONER_INDETERMINATE_SENTENCE,
  EXISTING_PRISONER_ON_REMAND,
  EXISTING_PRISONER_UN_SENTENCED,
}

enum class InductionScheduleStatus {
  SCHEDULED,
  COMPLETED,
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES,
  EXEMPT_PRISONER_FAILED_TO_ENGAGE,
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED,
  EXEMPT_PRISONER_SAFETY_ISSUES,
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
  EXEMPT_PRISON_STAFF_REDEPLOYMENT,
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE,
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF,
  EXEMPT_SYSTEM_TECHNICAL_ISSUE,
  EXEMPT_PRISONER_TRANSFER,
  EXEMPT_PRISONER_RELEASE,
  EXEMPT_PRISONER_DEATH,
  EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE,
  EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS,
}
