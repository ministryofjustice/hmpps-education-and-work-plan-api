package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
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
class PreviousQualificationsMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  var educationLevel: HighestEducationLevel? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var qualifications: MutableList<QualificationMigrationEntity>? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var createdByDisplayName: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  var updatedBy: String? = null,

  @Column
  var updatedByDisplayName: String? = null,
) : ParentMigrationEntity() {

  fun qualifications(): MutableList<QualificationMigrationEntity> {
    if (qualifications == null) {
      qualifications = mutableListOf()
    }
    return qualifications!!
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PreviousQualificationsMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, educationLevel = $educationLevel)"
  }
}

@Table(name = "qualification")
@Entity
class QualificationMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_qualifications_id")
  var parent: PreviousQualificationsMigrationEntity? = null,

  @field:NotNull
  var subject: String? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  var level: QualificationLevel? = null,

  @Column
  var grade: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var updatedAt: Instant? = null,

  @Column
  var updatedBy: String? = null,
) : KeyAwareChildMigrationEntity {

  override fun associateWithParent(parent: ParentMigrationEntity) {
    this.parent = parent as PreviousQualificationsMigrationEntity
  }

  override fun key(): String = subject!!

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as QualificationMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, subject = $subject, level = $level, grade = $grade)"
  }
}

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
  ENTRY_LEVEL,
  LEVEL_1,
  LEVEL_2,
  LEVEL_3,
  LEVEL_4,
  LEVEL_5,
  LEVEL_6,
  LEVEL_7,
  LEVEL_8,
}
