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

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

  @Column
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
  EXISTING_PRISONER,
}

enum class InductionScheduleStatus(val active: Boolean) {
  PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS(true),
  SCHEDULED(true),
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY(true),
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES(true),
  EXEMPT_PRISONER_FAILED_TO_ENGAGE(true),
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED(true),
  EXEMPT_PRISONER_SAFETY_ISSUES(true),
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES(true),
  EXEMPT_PRISON_STAFF_REDEPLOYMENT(true),
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE(true),
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF(true),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE(true),
  EXEMPT_PRISONER_TRANSFER(true),
  EXEMPT_PRISONER_RELEASE(false),
  EXEMPT_PRISONER_DEATH(false),
  EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE(true),
  EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS(true),
  COMPLETED(false),
  ;

  companion object {
    val ACTIVE_STATUSES = InductionScheduleStatus.entries.filter { it.active }
  }
}
