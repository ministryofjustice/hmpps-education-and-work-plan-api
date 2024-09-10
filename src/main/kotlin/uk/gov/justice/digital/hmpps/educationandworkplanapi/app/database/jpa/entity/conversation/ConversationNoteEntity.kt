package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Table(name = "conversation_note")
@Entity
class ConversationNoteEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @field:NotNull
  var content: String? = null,

  @Column(updatable = false)
  var createdAt: Instant? = null,

  @Column
  @field:NotNull
  var createdAtPrison: String? = null,

  @Column(updatable = false)
  var createdBy: String? = null,

  @Column
  var createdByDisplayName: String? = null,

  @Column
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
    other as ConversationNoteEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference)"
  }
}
