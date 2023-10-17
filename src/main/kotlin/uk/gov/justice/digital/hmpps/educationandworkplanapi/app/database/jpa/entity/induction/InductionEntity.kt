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
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.CreatedByDisplayName
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.LastModifiedByDisplayName
import java.time.Instant
import java.util.UUID

/**
 * Represents a Prisoner's Induction, which is typically carried out by a CIAG officer shortly after the Prisoner
 * has entered a Prison (either when starting a new sentence, or after being transferred from another Prison).
 */
@Table(name = "induction")
@Entity
@EntityListeners(AuditingEntityListener::class)
class InductionEntity(
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
  @JoinColumn(name = "id")
  @field:NotNull
  var workOnRelease: WorkOnReleaseEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var previousQualifications: PreviousQualificationsEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var previousTraining: PreviousTrainingEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var previousWorkExperiences: PreviousWorkExperiencesEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var inPrisonInterests: InPrisonInterestsEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var personalSkillsAndInterests: PersonalSkillsAndInterestsEntity? = null,

  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  var futureWorkInterestsEntity: FutureWorkInterestsEntity? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @CreatedByDisplayName
  var createdByDisplayName: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @field:NotNull
  var updatedAtPrison: String? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,

  @Column
  @LastModifiedByDisplayName
  var updatedByDisplayName: String? = null,
)
