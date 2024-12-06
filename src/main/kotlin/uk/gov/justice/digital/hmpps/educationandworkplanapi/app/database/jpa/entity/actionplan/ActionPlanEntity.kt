package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Table(name = "action_plan")
@Entity
@EntityListeners(AuditingEntityListener::class)
class ActionPlanEntity(
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

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", nullable = false)
  @OrderBy(value = "createdAt")
  @field:NotNull
  var goals: MutableList<GoalEntity>? = null,

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null,

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null,

  @Column
  @UpdateTimestamp
  var updatedAt: Instant? = null,

  @Column
  @LastModifiedBy
  var updatedBy: String? = null,
) {

  companion object {

    /**
     * Returns new un-persisted [ActionPlanEntity] for the specified prisoner with an empty collection of [GoalEntity]s
     */
    fun newActionPlanForPrisoner(prisonNumber: String): ActionPlanEntity =
      ActionPlanEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        goals = mutableListOf(),
      )
  }

  /**
   * Returns the [GoalEntity] identified by its reference from this [ActionPlanEntity] instance.
   * Returns null if the Goal Entity cannot be found.
   */
  fun getGoalByReference(goalReference: UUID): GoalEntity? =
    goals?.find { it.reference == goalReference }

  /**
   * Adds a [GoalEntity] to this [ActionPlanEntity]
   */
  fun addGoal(goalEntity: GoalEntity): ActionPlanEntity {
    goals!!.add(goalEntity)
    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as ActionPlanEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, prisonNumber = $prisonNumber)"
  }
}
