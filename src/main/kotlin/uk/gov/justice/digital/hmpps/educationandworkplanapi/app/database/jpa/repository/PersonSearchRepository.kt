package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerActionDto

@Repository
interface PersonSearchRepository : JpaRepository<ActionPlanEntity, String> {

  @Query(
    nativeQuery = true,
    value = """
        SELECT 
            fp.prison_number,
            CASE WHEN a.prison_number IS NOT NULL THEN true ELSE false END AS has_action_plan,
            a.updated_at AS action_plan_updated_at,
            COALESCE(i.deadline_date, r.latest_review_date) AS next_action_date,
            CASE 
                WHEN i.deadline_date IS NOT NULL THEN 'induction'
                WHEN r.latest_review_date IS NOT NULL THEN 'review'
            END AS next_action_type
        FROM (
            SELECT DISTINCT prison_number FROM (
                SELECT prison_number FROM review_schedule WHERE schedule_status = 'SCHEDULED'
                UNION
                SELECT prison_number FROM induction_schedule WHERE schedule_status = 'SCHEDULED'
                UNION
                SELECT prison_number FROM action_plan
            ) AS prisonNumbers
            WHERE prison_number IN (:prisonNumbers)
        ) fp
        LEFT JOIN (
            SELECT * FROM review_schedule WHERE schedule_status = 'SCHEDULED'
        ) r ON fp.prison_number = r.prison_number
        LEFT JOIN (
            SELECT * FROM induction_schedule WHERE schedule_status = 'SCHEDULED'
        ) i ON fp.prison_number = i.prison_number
        LEFT JOIN action_plan a ON fp.prison_number = a.prison_number
        """,
  )
  fun additionalSearchData(@Param("prisonNumbers") prisonNumbers: List<String>): List<PrisonerActionDto>
}
