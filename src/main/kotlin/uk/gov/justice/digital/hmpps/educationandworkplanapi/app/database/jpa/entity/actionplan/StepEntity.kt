package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

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

@Table(name = "step")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class StepEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  var title: String,

  @Column
  @Enumerated(value = EnumType.STRING)
  var status: StepStatus,

  @Column
  var sequenceNumber: Int,

) : KeyAwareChildEntity {
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "goal_id")
  var parent: GoalEntity? = null

  @PrePersist
  @PreUpdate
  @PreRemove
  fun onChange() {
    parent?.childEntityUpdated()
  }

  override fun associateWithParent(parent: ParentEntity) {
    this.parent = parent as GoalEntity
  }

  override fun key(): String = reference.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as StepEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, title = $title)"
}

enum class StepStatus {
  NOT_STARTED,
  ACTIVE,
  COMPLETE,
}
