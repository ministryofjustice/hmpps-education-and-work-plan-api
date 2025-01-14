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
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.KeyAwareChildEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ParentEntity
import java.time.Instant
import java.util.UUID

/**
 * Holds details about a Prisoner's educational qualifications, including where relevant, the grades achieved in each
 * subject.
 *
 */
@Table(name = "previous_qualifications")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class PreviousQualificationsEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @Column
  @Enumerated(value = EnumType.STRING)
  var educationLevel: EducationLevel,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val qualifications: MutableList<QualificationEntity> = mutableListOf(),

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) : ParentEntity {

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

  override fun childEntityUpdated() {
    this.updatedAt = Instant.now()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PreviousQualificationsEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, educationLevel = $educationLevel)"
  }
}

@Table(name = "qualification")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class QualificationEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  var subject: String,

  @Column
  @Enumerated(value = EnumType.STRING)
  var level: QualificationLevel,

  @Column
  var grade: String,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) : KeyAwareChildEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_qualifications_id")
  var parent: PreviousQualificationsEntity? = null

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

  @PrePersist
  @PreUpdate
  @PreRemove
  fun onChange() {
    parent?.childEntityUpdated()
  }

  override fun associateWithParent(parent: ParentEntity) {
    this.parent = parent as PreviousQualificationsEntity
  }

  override fun key(): String = "${subject.trim()},$level".uppercase()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as QualificationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, subject = $subject, level = $level, grade = $grade)"
  }
}

enum class EducationLevel {
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
