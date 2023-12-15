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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aFullyPopulatedCreateInductionRequest

@ExtendWith(MockitoExtension::class)
class InductionResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: InductionResourceMapper

  @Mock
  private lateinit var workOnReleaseMapper: WorkOnReleaseResourceMapper

  @Mock
  private lateinit var qualificationsMapper: QualificationsResourceMapper

  @Mock
  private lateinit var previousTrainingMapper: PreviousTrainingResourceMapper

  @Mock
  private lateinit var workExperiencesMapper: WorkExperiencesResourceMapper

  @Mock
  private lateinit var inPrisonInterestsMapper: InPrisonInterestsResourceMapper

  @Mock
  private lateinit var skillsAndInterestsMapper: SkillsAndInterestsResourceMapper

  @Mock
  private lateinit var workInterestsMapper: WorkInterestsResourceMapper

  @Test
  fun `should map to CreateInductionDto`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val createInductionRequest = aFullyPopulatedCreateInductionRequest(prisonId = prisonId)
    val workOnRelease = aValidCreateWorkOnReleaseDto()
    val workExperiences = aValidCreatePreviousWorkExperiencesDto()
    val skillsAndInterests = aValidCreatePersonalSkillsAndInterestsDto()
    val qualifications = aValidCreatePreviousQualificationsDto()
    val training = aValidCreatePreviousTrainingDto()
    val inPrisonInterests = aValidCreateInPrisonInterestsDto()
    val workInterests = aValidCreateFutureWorkInterestsDto()
    val expectedCreateInductionDto = aValidCreateInductionDto(
      prisonNumber = prisonNumber,
      workOnRelease = workOnRelease,
      previousQualifications = qualifications,
      previousTraining = training,
      previousWorkExperiences = workExperiences,
      inPrisonInterests = inPrisonInterests,
      personalSkillsAndInterests = skillsAndInterests,
      futureWorkInterests = workInterests,
      prisonId = prisonId,
    )

    given(workOnReleaseMapper.toCreateWorkOnReleaseDto(any(), any())).willReturn(workOnRelease)
    given(qualificationsMapper.toCreatePreviousQualificationsDto(any(), any())).willReturn(qualifications)
    given(previousTrainingMapper.toCreatePreviousTrainingDto(any(), any())).willReturn(training)
    given(workExperiencesMapper.toCreatePreviousWorkExperiencesDto(any(), any())).willReturn(workExperiences)
    given(inPrisonInterestsMapper.toCreateInPrisonInterestsDto(any(), any())).willReturn(inPrisonInterests)
    given(skillsAndInterestsMapper.toCreatePersonalSkillsAndInterestsDto(any(), any())).willReturn(skillsAndInterests)
    given(workInterestsMapper.toCreateFutureWorkInterestsDto(any(), any())).willReturn(workInterests)

    // When
    val actual = mapper.toCreateInductionDto(prisonNumber, createInductionRequest)

    // Then
    assertThat(actual).isEqualTo(expectedCreateInductionDto)
  }
}
