package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction

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
 * Represents any in-prison work or training interests a Prisoner might have during their time in prison.
 */
@Table(name = "in_prison_interests")
@Entity
class InPrisonInterestsMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var inPrisonWorkInterests: MutableList<InPrisonWorkInterestMigrationEntity>? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestMigrationEntity>? = null,

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

  fun inPrisonWorkInterests(): MutableList<InPrisonWorkInterestMigrationEntity> {
    if (inPrisonWorkInterests == null) {
      inPrisonWorkInterests = mutableListOf()
    }
    return inPrisonWorkInterests!!
  }

  fun inPrisonTrainingInterests(): MutableList<InPrisonTrainingInterestMigrationEntity> {
    if (inPrisonTrainingInterests == null) {
      inPrisonTrainingInterests = mutableListOf()
    }
    return inPrisonTrainingInterests!!
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InPrisonInterestsMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}

@Table(name = "in_prison_work_interest")
@Entity
class InPrisonWorkInterestMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id")
  var parent: InPrisonInterestsMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var workType: InPrisonWorkType? = null,

  @Column
  var workTypeOther: String? = null,

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
    this.parent = parent as InPrisonInterestsMigrationEntity
  }

  override fun key(): String = workType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InPrisonWorkInterestMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, workType = $workType, workTypeOther = $workTypeOther)"
  }
}

@Table(name = "in_prison_training_interest")
@Entity
class InPrisonTrainingInterestMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interests_id")
  var parent: InPrisonInterestsMigrationEntity? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var trainingType: InPrisonTrainingType? = null,

  @Column
  var trainingTypeOther: String? = null,

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
    this.parent = parent as InPrisonInterestsMigrationEntity
  }

  override fun key(): String = trainingType!!.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InPrisonTrainingInterestMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, trainingType = $trainingType, trainingTypeOther = $trainingTypeOther)"
  }
}

enum class InPrisonWorkType {
  CLEANING_AND_HYGIENE,
  COMPUTERS_OR_DESK_BASED,
  GARDENING_AND_OUTDOORS,
  KITCHENS_AND_COOKING,
  MAINTENANCE,
  PRISON_LAUNDRY,
  PRISON_LIBRARY,
  TEXTILES_AND_SEWING,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}

enum class InPrisonTrainingType {
  BARBERING_AND_HAIRDRESSING,
  CATERING,
  COMMUNICATION_SKILLS,
  ENGLISH_LANGUAGE_SKILLS,
  FORKLIFT_DRIVING,
  INTERVIEW_SKILLS,
  MACHINERY_TICKETS,
  NUMERACY_SKILLS,
  RUNNING_A_BUSINESS,
  SOCIAL_AND_LIFE_SKILLS,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}
