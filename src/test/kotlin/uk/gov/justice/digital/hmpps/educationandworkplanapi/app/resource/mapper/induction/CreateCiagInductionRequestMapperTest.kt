package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest

@ExtendWith(MockitoExtension::class)
class CreateCiagInductionRequestMapperTest {
  @InjectMocks
  private lateinit var mapper: CreateCiagInductionRequestMapper

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
  private lateinit var workInterestsMapper: FutureWorkInterestsResourceMapper

  @Test
  fun `should map to createInductionDto`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val request = aValidCreateCiagInductionRequest(prisonId = prisonId)

    given(workOnReleaseMapper.toCreateWorkOnReleaseDto(any())).willReturn(aValidCreateWorkOnReleaseDto())
    given(qualificationsAndTrainingMapper.toCreatePreviousQualificationsDto(any(), any())).willReturn(aValidCreatePreviousQualificationsDto())
    given(qualificationsAndTrainingMapper.toCreatePreviousTrainingDto(any(), any())).willReturn(aValidCreatePreviousTrainingDto())
    given(workExperiencesMapper.toCreatePreviousWorkExperiencesDto(any(), any())).willReturn(aValidCreatePreviousWorkExperiencesDto())
    given(inPrisonInterestsMapper.toCreateInPrisonInterestsDto(any(), any())).willReturn(aValidCreateInPrisonInterestsDto())
    given(skillsAndInterestsMapper.toCreatePersonalSkillsAndInterestsDto(any(), any())).willReturn(aValidCreatePersonalSkillsAndInterestsDto())
    given(workInterestsMapper.toCreateFutureWorkInterestsDto(any(), any())).willReturn(aValidCreateFutureWorkInterestsDto())

    // When
    val actual = mapper.toCreateInductionDto(prisonNumber, request)

    // Then
    assertThat(actual.prisonNumber).isEqualTo(prisonNumber)
    assertThat(actual.prisonId).isEqualTo(prisonId)
    verify(workOnReleaseMapper).toCreateWorkOnReleaseDto(request)
    verify(qualificationsAndTrainingMapper).toCreatePreviousQualificationsDto(request.qualificationsAndTraining, prisonId)
    verify(qualificationsAndTrainingMapper).toCreatePreviousTrainingDto(request.qualificationsAndTraining, prisonId)
    verify(workExperiencesMapper).toCreatePreviousWorkExperiencesDto(request.workExperience, prisonId)
    verify(inPrisonInterestsMapper).toCreateInPrisonInterestsDto(request.inPrisonInterests, prisonId)
    verify(skillsAndInterestsMapper).toCreatePersonalSkillsAndInterestsDto(request.skillsAndInterests, prisonId)
    verify(workInterestsMapper).toCreateFutureWorkInterestsDto(request.workExperience!!.workInterests, prisonId)
  }
}
