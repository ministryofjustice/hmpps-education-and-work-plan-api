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
 * Contains details of a Prisoner's work experience, if applicable.
 *
 * Note that if the list of `experiences` is empty, then the Prisoner has been asked if they have any work history,
 * but either they do not, or they do not wish to provide details.
 */
@Table(name = "previous_work_experiences")
@Entity
class PreviousWorkExperiencesMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var experiences: MutableList<WorkExperienceMigrationEntity>? = null,

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

  fun experiences(): MutableList<WorkExperienceMigrationEntity> {
    if (experiences == null) {
      experiences = mutableListOf()
    }
    return experiences!!
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as PreviousWorkExperiencesMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "work_experience")
@Entity
class WorkExperienceMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_experiences_id")
  var parent: PreviousWorkExperiencesMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var experienceType: WorkExperienceType? = null,

  @Column
  var experienceTypeOther: String? = null,

  @Column
  var role: String? = null,

  @Column
  var details: String? = null,

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
    this.parent = parent as PreviousWorkExperiencesMigrationEntity
  }

  override fun key(): String = experienceType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as WorkExperienceMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, experienceType = $experienceType, experienceTypeOther = $experienceTypeOther)"
  }
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
