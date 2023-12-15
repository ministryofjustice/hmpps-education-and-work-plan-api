package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCiagInductionSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidSkillsAndInterestsResponse
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@ExtendWith(MockitoExtension::class)
class CiagInductionResponseMapperTest {

  @InjectMocks
  private lateinit var mapper: CiagInductionResponseMapper

  @Mock
  private lateinit var workOnReleaseMapper: CiagWorkOnReleaseResourceMapper

  @Mock
  private lateinit var qualificationsAndTrainingMapper: QualificationsAndTrainingResourceMapper

  @Mock
  private lateinit var workExperiencesMapper: CiagWorkExperiencesResourceMapper

  @Mock
  private lateinit var inPrisonInterestsMapper: CiagInPrisonInterestsResourceMapper

  @Mock
  private lateinit var skillsAndInterestsMapper: CiagSkillsAndInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CiagInductionResponse given prisoner does not wish to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionDomain = aValidInduction(
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(
        hopingToWork = HopingToWorkDomain.NO,
        notHopingToWorkReasons = listOf(NotHopingToWorkReason.OTHER),
        notHopingToWorkOtherReason = "Crime pays",
        affectAbilityToWork = listOf(AffectAbilityToWork.OTHER),
        affectAbilityToWorkOther = "Mental health issues",
      ),
    )
    val workExperience = aValidPreviousWorkResponse()
    val skillsAndInterests = aValidSkillsAndInterestsResponse()
    val qualificationsAndTraining = aValidEducationAndQualificationsResponse()
    val inPrisonInterests = aValidPrisonWorkAndEducationResponse()
    val expectedInductionResponse = aValidCiagInductionResponse(
      offenderId = prisonNumber,
      hopingToGetWork = HopingToWorkApi.NO,
      desireToWork = false,
      abilityToWork = setOf(AbilityToWorkFactor.OTHER),
      abilityToWorkOther = "Mental health issues",
      reasonToNotGetWork = setOf(ReasonNotToWork.OTHER),
      reasonToNotGetWorkOther = "Crime pays",
      workExperience = workExperience,
      skillsAndInterests = skillsAndInterests,
      qualificationsAndTraining = qualificationsAndTraining,
      inPrisonInterests = inPrisonInterests,
    )

    given(workOnReleaseMapper.toReasonsNotToWork(any())).willReturn(setOf(ReasonNotToWork.OTHER))
    given(workOnReleaseMapper.toAbilityToWorkFactors(any())).willReturn(setOf(AbilityToWorkFactor.OTHER))
    given(qualificationsAndTrainingMapper.toEducationAndQualificationResponse(any(), any())).willReturn(qualificationsAndTraining)
    given(workExperiencesMapper.toPreviousWorkResponse(any(), any())).willReturn(workExperience)
    given(inPrisonInterestsMapper.toPrisonWorkAndEducationResponse(any())).willReturn(inPrisonInterests)
    given(skillsAndInterestsMapper.toSkillsAndInterestsResponse(any())).willReturn(skillsAndInterests)
    given(instantMapper.toOffsetDateTime(any())).willReturn(OffsetDateTime.now())

    // When
    val actual = mapper.fromDomainToModel(inductionDomain)

    // Then
    assertThat(actual).usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*id", ".*reference", ".*createdDateTime", ".*modifiedDateTime")
      .isEqualTo(expectedInductionResponse)
  }

  @Test
  fun `should map to CiagInductionResponse given prisoner wishes to work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionDomain = aValidInduction(
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(
        hopingToWork = HopingToWorkDomain.YES,
        notHopingToWorkReasons = emptyList(),
        notHopingToWorkOtherReason = null,
        affectAbilityToWork = emptyList(),
        affectAbilityToWorkOther = null,
      ),
    )
    val workExperience = aValidPreviousWorkResponse()
    val skillsAndInterests = aValidSkillsAndInterestsResponse()
    val qualificationsAndTraining = aValidEducationAndQualificationsResponse()
    val inPrisonInterests = aValidPrisonWorkAndEducationResponse()
    val expectedInductionResponse = aValidCiagInductionResponse(
      offenderId = prisonNumber,
      hopingToGetWork = HopingToWorkApi.YES,
      desireToWork = true,
      abilityToWork = emptySet(),
      abilityToWorkOther = null,
      reasonToNotGetWork = emptySet(),
      reasonToNotGetWorkOther = null,
      workExperience = workExperience,
      skillsAndInterests = skillsAndInterests,
      qualificationsAndTraining = qualificationsAndTraining,
      inPrisonInterests = inPrisonInterests,
    )

    given(workOnReleaseMapper.toReasonsNotToWork(any())).willReturn(emptySet())
    given(workOnReleaseMapper.toAbilityToWorkFactors(any())).willReturn(emptySet())
    given(qualificationsAndTrainingMapper.toEducationAndQualificationResponse(any(), any())).willReturn(qualificationsAndTraining)
    given(workExperiencesMapper.toPreviousWorkResponse(any(), any())).willReturn(workExperience)
    given(inPrisonInterestsMapper.toPrisonWorkAndEducationResponse(any())).willReturn(inPrisonInterests)
    given(skillsAndInterestsMapper.toSkillsAndInterestsResponse(any())).willReturn(skillsAndInterests)
    given(instantMapper.toOffsetDateTime(any())).willReturn(OffsetDateTime.now())

    // When
    val actual = mapper.fromDomainToModel(inductionDomain)

    // Then
    assertThat(actual).usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*id", ".*reference", ".*createdDateTime", ".*modifiedDateTime")
      .isEqualTo(expectedInductionResponse)
  }

  @Test
  fun `should map from domain summaries to model summaries`() {
    // Given
    val summary1 = aValidInductionSummary(
      prisonNumber = aValidPrisonNumber(),
      workOnRelease = aValidWorkOnRelease(
        hopingToWork = HopingToWorkDomain.YES,
      ),
    )
    val summary2 = aValidInductionSummary(
      prisonNumber = anotherValidPrisonNumber(),
      workOnRelease = aValidWorkOnRelease(
        hopingToWork = HopingToWorkDomain.NO,
      ),
    )

    val now = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(now)

    val expected = listOf(
      aValidCiagInductionSummaryResponse(
        offenderId = summary1.prisonNumber,
        hopingToGetWork = HopingToWorkApi.YES,
        desireToWork = true,
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
    verify(instantMapper).toOffsetDateTime(summary1.createdAt)
    verify(instantMapper).toOffsetDateTime(summary1.workOnRelease.lastUpdatedAt)
    verify(instantMapper).toOffsetDateTime(summary2.createdAt)
    verify(instantMapper).toOffsetDateTime(summary2.workOnRelease.lastUpdatedAt)
  }
}
