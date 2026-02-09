package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

@Table(name = "employability_skill")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class EmployabilitySkillEntity(

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column
  val id: UUID? = null,

  @Column
  val reference: UUID = UUID.randomUUID(),

  @Column
  val prisonNumber: String,

  @Enumerated(EnumType.STRING)
  @Column
  val skillType: EmployabilitySkillType,
  @Column
  val evidence: String,
  @Column(name = "rating_code")
  val ratingCode: String,

  /**
   * Convenience association to the rating lookup table.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "rating_code",
    referencedColumnName = "code",
    insertable = false,
    updatable = false,
  )
  val rating: EmployabilitySkillRatingEntity? = null,

  @Column
  val activityName: String,

  @Column
  val conversationDate: LocalDate? = null,

  @CreatedBy
  @Column
  var createdBy: String? = null,

  @CreationTimestamp
  @Column
  var createdAt: Instant? = null,

  @Column
  var createdAtPrison: String? = null,

  @LastModifiedBy
  @Column
  var updatedBy: String? = null,

  @UpdateTimestamp
  @Column
  var updatedAt: Instant? = null,

  @Column
  var updatedAtPrison: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as EmployabilitySkillEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id=$id, prisonNumber=$prisonNumber, skillType=$skillType, ratingCode=$ratingCode)"
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
