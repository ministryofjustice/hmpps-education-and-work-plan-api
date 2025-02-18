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
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ParentEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table(name = "goal")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class GoalEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  var title: String,

  @Column
  var targetCompletionDate: LocalDate,

  @Column
  @Enumerated(value = EnumType.STRING)
  var status: GoalStatus,

  @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var steps: MutableList<StepEntity>,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,

  @Column
  @Enumerated(value = EnumType.STRING)
  var archiveReason: ReasonToArchiveGoal? = null,

  @Transient
  var notes: String? = null,

  @Column
  var archiveReasonOther: String? = null,
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
    updatedAt = Instant.now()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as GoalEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, title = $title)"
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
