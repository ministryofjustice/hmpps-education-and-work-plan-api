package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assessment

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table(name = "education_assessment_event")
@Entity
data class EducationAssessmentEventEntity(

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
  val status: EducationAssessmentEventStatus,

  @Column
  val statusChangeDate: LocalDate,

  @Column
  val source: String,

  @Column
  val detailUrl: String?,

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
    other as EducationAssessmentEventEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id=$id, prisonNumber=$prisonNumber, status=$status, source=$source)"
}

enum class EducationAssessmentEventStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
