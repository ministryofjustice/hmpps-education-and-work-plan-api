package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * Holds details about a Prisoner's educational qualifications, including where relevant, the grades achieved in each
 * subject.
 *
 * Note that the list of `qualifications` can be empty, but `educationLevel` is mandatory (but only if the Prisoner has
 * been asked about their education).
 */
@Table(name = "previous_qualifications")
@Entity
@EntityListeners(AuditingEntityListener::class)
class PreviousQualificationsEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var educationLevel: HighestEducationLevel? = null,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_qualifications_id", nullable = false)
  var qualifications: List<QualificationEntity>? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
)

@Table(name = "qualification")
@Entity
@EntityListeners(AuditingEntityListener::class)
class QualificationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @field:NotNull
  var reference: UUID? = null,

  @field:NotNull
  var subject: String? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  var level: QualificationLevel? = null,

  @Column
  var grade: String? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
)

enum class HighestEducationLevel {
  PRIMARY_SCHOOL,
  SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  SECONDARY_SCHOOL_TOOK_EXAMS,
  FURTHER_EDUCATION_COLLEGE,
  UNDERGRADUATE_DEGREE_AT_UNIVERSITY,
  POSTGRADUATE_DEGREE_AT_UNIVERSITY,
  NOT_SURE,
}

enum class QualificationLevel {
  ENTRY_LEVEL_2,
  ENTRY_LEVEL_3,
  LEVEL_1,
  LEVEL_2,
  LEVEL_3,
  LEVEL_4,
  LEVEL_5,
  LEVEL_6,
  LEVEL_7,
  LEVEL_8,
}
