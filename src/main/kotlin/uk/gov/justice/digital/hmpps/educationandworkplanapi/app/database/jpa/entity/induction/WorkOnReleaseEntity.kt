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
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
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
@EntityListeners(AuditingEntityListener::class)
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
  @CollectionTable(name = "not_working_reasons")
  @Column(name = "reason")
  var notHopingToWorkReasons: List<NotHopingToWorkReason>? = null,

  @Column
  var notHopingToWorkOtherReason: String? = null,

  @ElementCollection(targetClass = AffectAbilityToWork::class)
  @Enumerated(value = EnumType.STRING)
  @CollectionTable(name = "affecting_ability_to_work")
  @Column(name = "affect")
  var affectAbilityToWork: List<AffectAbilityToWork>? = null,

  @Column
  var affectAbilityToWorkOther: String? = null,

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
)

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
