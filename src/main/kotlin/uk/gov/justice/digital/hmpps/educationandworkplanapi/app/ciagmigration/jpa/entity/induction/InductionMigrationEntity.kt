package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Represents a Prisoner's Induction, which is typically carried out by a CIAG officer shortly after the Prisoner
 * has entered a Prison (either when starting a new sentence, or after being transferred from another Prison).
 */
@Table(name = "induction")
@Entity
class InductionMigrationEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var prisonNumber: String? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "work_on_release_id")
  @field:NotNull
  var workOnRelease: WorkOnReleaseMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "previous_qualifications_id")
  var previousQualifications: PreviousQualificationsMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "previous_training_id")
  var previousTraining: PreviousTrainingMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "work_experiences_id")
  var previousWorkExperiences: PreviousWorkExperiencesMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "in_prison_interests_id")
  var inPrisonInterests: InPrisonInterestsMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_and_interests_id")
  var personalSkillsAndInterests: PersonalSkillsAndInterestsMigrationEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "future_work_interests_id")
  var futureWorkInterests: FutureWorkInterestsMigrationEntity? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var createdByDisplayName: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  var updatedBy: String? = null,

  @Column
  var updatedByDisplayName: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InductionMigrationEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, prisonNumber = $prisonNumber)"
  }
}
