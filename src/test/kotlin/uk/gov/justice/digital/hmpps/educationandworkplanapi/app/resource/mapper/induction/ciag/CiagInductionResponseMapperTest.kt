package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCiagInductionSummaryResponse
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@ExtendWith(MockitoExtension::class)
class CiagInductionResponseMapperTest {

  @InjectMocks
  private lateinit var mapper: CiagInductionResponseMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map from domain summaries to model summaries`() {
    // Given
    val summary1 = aValidInductionSummary(
      prisonNumber = randomValidPrisonNumber(),
    )
    val summary2 = aValidInductionSummary(
      prisonNumber = randomValidPrisonNumber(),
    )

    val now = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(now)

    val expected = listOf(
      aValidCiagInductionSummaryResponse(
        offenderId = summary1.prisonNumber,
        hopingToGetWork = HopingToWorkApi.NO,
        desireToWork = false,
        createdBy = summary1.createdBy,
        createdDateTime = now,
        modifiedBy = summary1.lastUpdatedBy,
        modifiedDateTime = now,
      ),
      aValidCiagInductionSummaryResponse(
        offenderId = summary2.prisonNumber,
        hopingToGetWork = HopingToWorkApi.NO,
        desireToWork = false,
        createdBy = summary2.createdBy,
        createdDateTime = now,
        modifiedBy = summary2.lastUpdatedBy,
        modifiedDateTime = now,
      ),
    )

    // When
    val actual = mapper.fromDomainToModel(listOf(summary1, summary2))

    // Then
    assertThat(actual).hasSize(2)
    assertThat(actual).isEqualTo(expected)
  }
}
