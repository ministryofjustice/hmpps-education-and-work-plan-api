package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table(name = "goal")
@Entity
class GoalEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  @NotNull
  var id: UUID? = null,

  @Column
  @NotNull
  var reference: UUID? = null,

  @Column
  @NotNull
  var title: String? = null,

  @Column
  @NotNull
  var reviewDate: LocalDate? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @NotNull
  var category: GoalCategory? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @NotNull
  var status: GoalStatus? = null,

  @Column
  var notes: String? = null,

  @OneToMany
  @JoinColumn(name = "goal_id")
  @NotNull
  var steps: List<StepEntity>? = null,

  @Column
  @CreationTimestamp
  @NotNull
  var createdAt: Instant? = null,

  @Column
  @UpdateTimestamp
  @NotNull
  var updatedAt: Instant? = null,
) {

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

enum class GoalCategory {
  WORK,
  PERSONAL_DEVELOPMENT,
  EDUCATION,
  RESETTLEMENT,
}

enum class GoalStatus {
  ACTIVE,
  COMPLETED,
  ARCHIVED,
}