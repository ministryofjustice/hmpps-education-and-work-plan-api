package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
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

@Table(name = "review")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class ReviewEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column(updatable = false)
  val prisonNumber: String,

  @Column(updatable = false)
  val deadlineDate: LocalDate,

  @Column(updatable = false)
  val completedDate: LocalDate,

  @Column(updatable = false)
  val conductedBy: String?,

  @Column(updatable = false)
  val conductedByRole: String?,

  @Column(updatable = false)
  val createdAtPrison: String,

  @Column(updatable = false)
  val updatedAtPrison: String,
) {
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null

  @Column(updatable = false)
  @CreatedBy
  var createdBy: String? = null

  @Column(updatable = false)
  @CreationTimestamp
  var createdAt: Instant? = null

  @Column(updatable = false)
  @LastModifiedBy
  var updatedBy: String? = null

  @Column(updatable = false)
  @UpdateTimestamp
  var updatedAt: Instant? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as ReviewEntity

    return id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
