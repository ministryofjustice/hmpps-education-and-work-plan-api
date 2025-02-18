package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * Holds details of a Prisoner's work aspirations, including any barriers affecting their work.
 */
@Table(name = "work_on_release")
@Entity
@EntityListeners(value = [AuditingEntityListener::class])
data class WorkOnReleaseEntity(
  @Column(updatable = false)
  val reference: UUID,

  @Column
  @Enumerated(value = EnumType.STRING)
  var hopingToWork: HopingToWork,

  @ElementCollection(targetClass = AffectAbilityToWork::class)
  @Enumerated(value = EnumType.STRING)
  @CollectionTable(name = "affecting_ability_to_work", joinColumns = [JoinColumn(name = "work_on_release_id")])
  @Column(name = "affect")
  val affectAbilityToWork: MutableList<AffectAbilityToWork> = mutableListOf(),

  @Column(name = "affecting_work_other")
  var affectAbilityToWorkOther: String? = null,

  @Column
  val createdAtPrison: String,

  @Column
  var updatedAtPrison: String,
) {

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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as WorkOnReleaseEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = this::class.simpleName + "(id = $id, reference = $reference, hopingToWork = $hopingToWork)"
}

enum class HopingToWork {
  YES,
  NO,
  NOT_SURE,
}

enum class AffectAbilityToWork {
  LIMITED_BY_OFFENCE,
  CARING_RESPONSIBILITIES,
  NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH,
  UNABLE_TO_WORK_DUE_TO_HEALTH,
  LACKS_CONFIDENCE_OR_MOTIVATION,
  REFUSED_SUPPORT_WITH_NO_REASON,
  RETIRED,
  NO_RIGHT_TO_WORK,
  NOT_SURE,
  OTHER,
  NONE,
}
