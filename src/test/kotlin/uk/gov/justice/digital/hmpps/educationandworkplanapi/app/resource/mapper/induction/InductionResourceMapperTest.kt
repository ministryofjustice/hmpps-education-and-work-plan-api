package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aFullyPopulatedCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aFullyPopulatedInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aFullyPopulatedUpdateInductionRequest
import java.time.ZoneOffset

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
    given(qualificationsMapper.toCreatePreviousQualificationsDto(any(), any(), any())).willReturn(qualifications)
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

  @Test
  fun `should map to InductionResponse`() {
    // Given
    val induction = aFullyPopulatedInduction()
    val expectedInduction = aFullyPopulatedInductionResponse(
      reference = induction.reference,
      prisonNumber = induction.prisonNumber,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      createdAt = induction.createdAt!!.atOffset(ZoneOffset.UTC),
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
      updatedAt = induction.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
    )

    given(workOnReleaseMapper.toWorkOnReleaseResponse(any())).willReturn(expectedInduction.workOnRelease)
    given(qualificationsMapper.toPreviousQualificationsResponse(any())).willReturn(expectedInduction.previousQualifications)
    given(previousTrainingMapper.toPreviousTrainingResponse(any())).willReturn(expectedInduction.previousTraining)
    given(workExperiencesMapper.toPreviousWorkExperiencesResponse(any())).willReturn(expectedInduction.previousWorkExperiences)
    given(inPrisonInterestsMapper.toInPrisonInterestsResponse(any())).willReturn(expectedInduction.inPrisonInterests)
    given(skillsAndInterestsMapper.toPersonalSkillsAndInterestsResponse(any())).willReturn(expectedInduction.personalSkillsAndInterests)
    given(workInterestsMapper.toFutureWorkInterestsResponse(any())).willReturn(expectedInduction.futureWorkInterests)

    // When
    val actual = mapper.toInductionResponse(induction)

    // Then
    assertThat(actual).isEqualTo(expectedInduction)
  }

  @Test
  fun `should map to UpdateInductionDto`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val updateRequest = aFullyPopulatedUpdateInductionRequest(prisonId = prisonId)
    val workOnRelease = aValidUpdateWorkOnReleaseDto()
    val workExperiences = aValidUpdatePreviousWorkExperiencesDto()
    val skillsAndInterests = aValidUpdatePersonalSkillsAndInterestsDto()
    val qualifications = aValidUpdatePreviousQualificationsDto()
    val training = aValidUpdatePreviousTrainingDto()
    val inPrisonInterests = aValidUpdateInPrisonInterestsDto()
    val workInterests = aValidUpdateFutureWorkInterestsDto()

    given(workOnReleaseMapper.toUpdateWorkOnReleaseDto(any(), any())).willReturn(workOnRelease)
    given(qualificationsMapper.toUpdatePreviousQualificationsDto(any(), any())).willReturn(qualifications)
    given(previousTrainingMapper.toUpdatePreviousTrainingDto(any(), any())).willReturn(training)
    given(workExperiencesMapper.toUpdatePreviousWorkExperiencesDto(any(), any())).willReturn(workExperiences)
    given(inPrisonInterestsMapper.toUpdateInPrisonInterestsDto(any(), any())).willReturn(inPrisonInterests)
    given(skillsAndInterestsMapper.toUpdatePersonalSkillsAndInterestsDto(any(), any())).willReturn(skillsAndInterests)
    given(workInterestsMapper.toUpdateFutureWorkInterestsDto(any(), any())).willReturn(workInterests)
    val expectedUpdateInductionDto = aValidUpdateInductionDto(
      updateRequest.reference,
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

    // When
    val actual = mapper.toUpdateInductionDto(prisonNumber, updateRequest)

    // Then
    assertThat(actual).isEqualTo(expectedUpdateInductionDto)
  }
}
