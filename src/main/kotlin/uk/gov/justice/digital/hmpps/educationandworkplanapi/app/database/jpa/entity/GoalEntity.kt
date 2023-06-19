package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.util.UUID

@Table(name = "goal")
@Entity
class GoalEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column
  var reference: UUID? = null,

  @Column
  var title: String? = null,

  @Column
  var reviewDate: LocalDate? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  var category: GoalCategory? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  var status: GoalStatus? = null,

  @Column
  var notes: String? = null,
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
