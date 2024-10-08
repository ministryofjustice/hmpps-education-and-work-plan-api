package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.CreatedByDisplayName
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.DisplayNameAuditingEntityListener.LastModifiedByDisplayName
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ParentEntity
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Table(name = "goal")
@Entity
@EntityListeners(value = [AuditingEntityListener::class, DisplayNameAuditingEntityListener::class])
class GoalEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @field:NotNull
  var title: String? = null,

  @Column
  @field:NotNull
  var targetCompletionDate: LocalDate? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var status: GoalStatus? = null,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @field:NotNull
  var steps: MutableList<StepEntity>? = null,

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

  @Column
  @Enumerated(value = EnumType.STRING)
  var archiveReason: ReasonToArchiveGoal? = null,

  @Transient
  var notes: String? = null,

  @Column
  var archiveReasonOther: String? = null,
) : ParentEntity {

  fun steps(): MutableList<StepEntity> {
    if (steps == null) {
      steps = mutableListOf()
    }
    return steps!!
  }

  override fun childEntityUpdated() {
    updatedAt = Instant.now()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as GoalEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, title = $title)"
  }
}

enum class GoalStatus {
  ACTIVE,
  COMPLETED,
  ARCHIVED,
}

enum class ReasonToArchiveGoal {
  PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
  PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG,
  SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON,
  OTHER,
}
