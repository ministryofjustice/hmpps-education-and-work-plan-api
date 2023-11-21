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
 * Represents a Prisoner's future work aspirations, including the type/sector of work and their desired role within it.
 */
@Table(name = "future_work_interests")
@Entity
class FutureWorkInterestsMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var interests: MutableList<WorkInterestMigrationEntity>? = null,

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

  fun interests(): MutableList<WorkInterestMigrationEntity> {
    if (interests == null) {
      interests = mutableListOf()
    }
    return interests!!
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as FutureWorkInterestsMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "work_interest")
@Entity
class WorkInterestMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_interests_id")
  var parent: FutureWorkInterestsMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var workType: WorkInterestType? = null,

  @Column
  var workTypeOther: String? = null,

  @Column
  var role: String? = null,

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
    this.parent = parent as FutureWorkInterestsMigrationEntity
  }

  override fun key(): String = workType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as WorkInterestMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, workType = $workType, workTypeOther = $workTypeOther)"
  }
}

enum class WorkInterestType {
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
