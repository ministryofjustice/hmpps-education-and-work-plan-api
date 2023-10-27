package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationResponse
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
  private lateinit var workOnReleaseMapper: WorkOnReleaseResourceMapper

  @Mock
  private lateinit var qualificationsAndTrainingMapper: QualificationsAndTrainingResourceMapper

  @Mock
  private lateinit var workExperiencesMapper: PreviousWorkExperiencesResourceMapper

  @Mock
  private lateinit var inPrisonInterestsMapper: InPrisonInterestsResourceMapper

  @Mock
  private lateinit var skillsAndInterestsMapper: PersonalSkillsAndInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CiagInductionResponse`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionDomain = aValidInduction(
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(
        hopingToWork = HopingToWorkDomain.NOT_SURE,
        notHopingToWorkReasons = listOf(NotHopingToWorkReason.OTHER),
        notHopingToWorkOtherReason = "Crime pays",
        affectAbilityToWork = listOf(AffectAbilityToWork.OTHER),
        affectAbilityToWorkOther = "Mental health issues",
      ),
    )
    val workExperience = aValidPreviousWorkResponse()
    val skillsAndInterests = aValidSkillsAndInterestsResponse()
    val qualificationsAndTraining = aValidEducationAndQualificationResponse()
    val inPrisonInterests = aValidPrisonWorkAndEducationResponse()
    val expectedInductionResponse = aValidCiagInductionResponse(
      offenderId = prisonNumber,
      hopingToGetWork = HopingToWorkApi.NOT_SURE,
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
}
