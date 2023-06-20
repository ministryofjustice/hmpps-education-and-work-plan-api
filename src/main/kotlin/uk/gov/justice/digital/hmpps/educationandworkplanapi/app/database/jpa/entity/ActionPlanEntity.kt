package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Table(name = "action_plan")
@Entity
class ActionPlanEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  @NotNull
  var id: UUID? = null,

  @Column(updatable = false)
  @NotNull
  var prisonNumber: String? = null,

  @OneToMany
  @JoinColumn(name = "action_plan_id")
  @NotNull
  var goals: MutableList<GoalEntity>? = null,
) {

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
