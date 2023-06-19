package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalCategory as EntityCategory
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalStatus as EntityStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalCategory as DomainCategory
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus as DomainStatus

class GoalEntityMapperTest {

  private val mapper = GoalEntityMapperImpl()

  @Test
  fun `should map from domain to entity`() {
    // Given
    val domainGoal = aValidGoal(
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )

    val expected = aValidGoalEntity(
      id = null,
      reference = domainGoal.reference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )

    // When
    val actual = mapper.fromDomainToEntity(domainGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val entityGoal = aValidGoalEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )

    val expected = aValidGoal(
      reference = entityGoal.reference!!,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )

    // When
    val actual = mapper.fromEntityToDomain(entityGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
