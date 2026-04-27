package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill

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
import java.util.UUID

@Table(name = "employability_skill")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class EmployabilitySkillEntity(
  @Column(updatable = false)
  val reference: UUID = UUID.randomUUID(),

  @Column(updatable = false)
  val prisonNumber: String,

  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  val skillType: EmployabilitySkillType,

  @Column(updatable = false)
  val evidence: String,

  @Enumerated(EnumType.STRING)
  @Column(updatable = false, name = "rating_code")
  val rating: EmployabilitySkillRating,

  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  val sessionType: EmployabilitySkillSessionType?,

  @Column(updatable = false)
  val sessionTypeDescription: String?,

  @Column(updatable = false)
  val createdAtPrison: String? = null,

  @Column
  var updatedAtPrison: String? = null,
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
    other as EmployabilitySkillEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id=$id, prisonNumber=$prisonNumber, skillType=$skillType, rating=$rating)"
}

enum class EmployabilitySkillType {
  TEAMWORK,
  TIMEKEEPING,
  COMMUNICATION,
  PLANNING,
  ORGANISATION,
  PROBLEM_SOLVING,
  INITIATIVE,
  ADAPTABILITY,
  RELIABILITY,
  CREATIVITY,
}

enum class EmployabilitySkillRating {
  NOT_CONFIDENT,
  LITTLE_CONFIDENCE,
  QUITE_CONFIDENT,
  VERY_CONFIDENT,
}

enum class EmployabilitySkillSessionType {
  CIAG_INDUCTION,
  CIAG_REVIEW,
  EDUCATION_REVIEW,
  INDUSTRIES_REVIEW,
}
