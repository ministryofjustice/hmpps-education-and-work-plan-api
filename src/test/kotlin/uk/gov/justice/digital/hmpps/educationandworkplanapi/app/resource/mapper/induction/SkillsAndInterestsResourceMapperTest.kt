package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPersonalSkillsAndInterestsResponse
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class SkillsAndInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: SkillsAndInterestsResourceMapperImpl

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreatePersonalSkillsAndInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePersonalSkillsAndInterestsRequest()
    val expected = aValidCreatePersonalSkillsAndInterestsDto(
      skills = listOf(
        aValidPersonalSkill(
          skillType = SkillType.OTHER,
          skillTypeOther = "Hidden skills",
        ),
      ),
      interests = listOf(
        aValidPersonalInterest(
          interestType = InterestType.OTHER,
          interestTypeOther = "Varied interests",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePersonalSkillsAndInterestsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PersonalSkillsAndInterestsResponse`() {
    // Given
    val domain = aValidPersonalSkillsAndInterests()
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidPersonalSkillsAndInterestsResponse(
      reference = domain.reference,
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toPersonalSkillsAndInterestsResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
