package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review

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

@Table(name = "review_schedule")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class ReviewScheduleEntity(
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

  @Column
  var exemptionReason: String?,

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
    other as ReviewScheduleEntity

    return id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}

enum class ReviewScheduleCalculationRule {
  PRISONER_READMISSION,
  PRISONER_TRANSFER,
  BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
  BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
  BETWEEN_3_AND_6_MONTHS_TO_SERVE,
  BETWEEN_6_AND_12_MONTHS_TO_SERVE,
  BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  MORE_THAN_60_MONTHS_TO_SERVE,
  INDETERMINATE_SENTENCE,
  PRISONER_ON_REMAND,
  PRISONER_UN_SENTENCED,
}

enum class ReviewScheduleStatus {
  SCHEDULED,
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
  EXEMPT_UNKNOWN,
  COMPLETED,
}
