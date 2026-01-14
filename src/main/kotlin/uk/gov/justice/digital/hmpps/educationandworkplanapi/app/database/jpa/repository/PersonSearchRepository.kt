package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AdditionalPrisonerDataDto

@Repository
interface PersonSearchRepository : JpaRepository<ActionPlanEntity, String> {

  @Query(
    value = """
    select
      p.prison_number as prisonNumber,
      case
        when exists (
          select 1
          from action_plan ap
          where ap.prison_number = p.prison_number
            and exists (
              select 1
              from goal g
              where g.action_plan_id = ap.id
            )
        ) then 'ACTIVE_PLAN'
        when exists (
          select 1
          from induction_schedule isch
          where isch.prison_number = p.prison_number
            and isch.schedule_status ilike '%EXEMPT%'
        ) then 'EXEMPT'
        else 'NEEDS_PLAN'
      end as planStatus
    from (
      select prison_number
      from action_plan
      where prison_number in (:prisonNumbers)

      union

      select prison_number
      from induction_schedule
      where prison_number in (:prisonNumbers)
    ) p
  """,
    nativeQuery = true,
  )
  fun additionalSearchData(@Param("prisonNumbers") prisonNumbers: List<String>): List<AdditionalPrisonerDataDto>
}
