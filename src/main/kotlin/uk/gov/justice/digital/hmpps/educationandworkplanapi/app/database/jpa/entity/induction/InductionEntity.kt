package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
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

/**
 * Represents a Prisoner's Induction, which is typically carried out by a CIAG officer shortly after the Prisoner
 * has entered a Prison (either when starting a new sentence, or after being transferred from another Prison).
 */
@Table(name = "induction")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class InductionEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "work_on_release_id")
  val workOnRelease: WorkOnReleaseEntity,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "previous_training_id")
  val previousTraining: PreviousTrainingEntity,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "work_experiences_id")
  var previousWorkExperiences: PreviousWorkExperiencesEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "in_prison_interests_id")
  var inPrisonInterests: InPrisonInterestsEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "skills_and_interests_id")
  var personalSkillsAndInterests: PersonalSkillsAndInterestsEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "future_work_interests_id")
  var futureWorkInterests: FutureWorkInterestsEntity? = null,

  @Column(updatable = false)
  val conductedBy: String? = null,

  @Column(updatable = false)
  val conductedByRole: String? = null,

  @Column(updatable = false)
  var completedDate: LocalDate? = null,

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

  fun updateLastUpdatedAt() {
    updatedAt = Instant.now()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as InductionEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, prisonNumber = $prisonNumber)"
}
