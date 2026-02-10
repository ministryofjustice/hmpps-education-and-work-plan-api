package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Table(name = "employability_skill_rating")
@Entity
@Immutable
data class EmployabilitySkillRatingEntity(

  @Id
  @Column
  val code: String,

  @Column
  val description: String? = null,

  @Column
  val score: Int? = null,
)
