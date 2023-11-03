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
import java.time.Instant
import java.util.UUID

/**
 * Holds details of a Prisoner's work aspirations, including any barriers affecting their work.
 */
@Table(name = "work_on_release")
@Entity
@EntityListeners(value = [AuditingEntityListener::class, DisplayNameAuditingEntityListener::class])
class WorkOnReleaseEntity(
  @Id
  @GeneratedValue
  @UuidGenerator
  var id: UUID? = null,

  @Column(updatable = false)
  @field:NotNull
  var reference: UUID? = null,

  @Column
  @Enumerated(value = EnumType.STRING)
  @field:NotNull
  var hopingToWork: HopingToWork? = null,

  @ElementCollection(targetClass = NotHopingToWorkReason::class)
  @Enumerated(value = EnumType.STRING)
  @CollectionTable(name = "not_working_reasons", joinColumns = [JoinColumn(name = "work_on_release_id")])
  @Column(name = "reason")
  var notHopingToWorkReasons: MutableList<NotHopingToWorkReason>? = null,

  @Column(name = "not_hoping_other")
  var notHopingToWorkOtherReason: String? = null,

  @ElementCollection(targetClass = AffectAbilityToWork::class)
  @Enumerated(value = EnumType.STRING)
  @CollectionTable(name = "affecting_ability_to_work", joinColumns = [JoinColumn(name = "work_on_release_id")])
  @Column(name = "affect")
  var affectAbilityToWork: MutableList<AffectAbilityToWork>? = null,

  @Column(name = "affecting_work_other")
  var affectAbilityToWorkOther: String? = null,

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
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as WorkOnReleaseEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(id = $id, reference = $reference, hopingToWork = $hopingToWork)"
  }
}

enum class HopingToWork {
  YES,
  NO,
  NOT_SURE,
}

enum class NotHopingToWorkReason {
  LIMIT_THEIR_ABILITY,
  FULL_TIME_CARER,
  LACKS_CONFIDENCE_OR_MOTIVATION,
  HEALTH,
  RETIRED,
  NO_RIGHT_TO_WORK,
  NOT_SURE,
  OTHER,
  NO_REASON,
}

enum class AffectAbilityToWork {
  CARING_RESPONSIBILITIES,
  LIMITED_BY_OFFENSE,
  HEALTH_ISSUES,
  NO_RIGHT_TO_WORK,
  OTHER,
  NONE,
}
