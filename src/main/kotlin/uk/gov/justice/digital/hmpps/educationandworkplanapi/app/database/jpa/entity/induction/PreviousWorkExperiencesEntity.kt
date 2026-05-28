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
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * Contains details of a Prisoner's work experience, if applicable.
 *
 * Note that if the list of `experiences` is empty, then the Prisoner has been asked if they have any work history,
 * but either they do not, or they do not wish to provide details.
 */
@Table(name = "previous_work_experiences")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class PreviousWorkExperiencesEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Enumerated(value = EnumType.STRING)
  var hasWorkedBefore: HasWorkedBefore,

  @Column
  var hasWorkedBeforeNotRelevantReason: String? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val experiences: MutableList<WorkExperienceEntity> = mutableListOf(),

  @Column
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreatedDate
  var createdAt: Instant? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column
  @LastModifiedDate
  var updatedAt: Instant? = null

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

  @Column
  @Version
  var version: Long = 0

  fun addChildren(newChildren: List<WorkExperienceEntity>) {
    newChildren.forEach {
      it.associateWithParent(this)
      experiences.add(it)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PreviousWorkExperiencesEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference)"
}

@Table(name = "work_experience")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class WorkExperienceEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var experienceType: WorkExperienceType,

  @Column
  var experienceTypeOther: String? = null,

  @Column
  var role: String? = null,

  @Column
  var details: String? = null,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_experiences_id")
  var parent: PreviousWorkExperiencesEntity? = null

  @Column(updatable = false)
  @CreatedDate
  var createdAt: Instant? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column
  @LastModifiedDate
  var updatedAt: Instant? = null

  @Column
  @LastModifiedBy
  var updatedBy: String? = null

  @PrePersist
  @PreUpdate
  @PreRemove
  fun onChangeTouchParentToUpdateItsTimestamp() {
    parent?.run { ++version } // Increment the parent's version field which will dirty the entity, forcing JPA to update the updated_at timestamp of the parent entity
  }

  fun associateWithParent(parent: PreviousWorkExperiencesEntity) {
    this.parent = parent
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as WorkExperienceEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, experienceType = $experienceType, experienceTypeOther = $experienceTypeOther)"
}

enum class WorkExperienceType {
  OUTDOOR,
  CONSTRUCTION,
  DRIVING,
  BEAUTY,
  HOSPITALITY,
  TECHNICAL,
  MANUFACTURING,
  OFFICE,
  RETAIL,
  SPORTS,
  WAREHOUSING,
  WASTE_MANAGEMENT,
  EDUCATION_TRAINING,
  CLEANING_AND_MAINTENANCE,
  OTHER,
}

enum class HasWorkedBefore {
  YES,
  NO,
  NOT_RELEVANT,
}
