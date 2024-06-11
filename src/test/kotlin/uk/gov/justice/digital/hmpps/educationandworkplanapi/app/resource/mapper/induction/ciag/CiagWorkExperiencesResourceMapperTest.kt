package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidCreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidUpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidWorkExperienceResource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag.aValidWorkInterestsResponse
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore as HasWorkedBeforeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType as WorkExperienceTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType as WorkInterestTypeDomain

class CiagWorkExperiencesResourceMapperTest {
  private val mapper = CiagWorkExperiencesResourceMapperImpl()

  @Test
  fun `should map to CreatePreviousWorkExperiencesDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousWorkRequest()
    val expectedDto = aValidCreatePreviousWorkExperiencesDto(
      hasWorkedBefore = HasWorkedBeforeDomain.YES,
      experiences = listOf(
        WorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "Scientist",
          role = "Lab Technician",
          details = "Cleaning test tubes",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expectedDto)
  }

  @Test
  fun `should map to PreviousWorkResponse`() {
    // Given
    val workExperiences = aValidPreviousWorkExperiences(
      hasWorkedBefore = HasWorkedBeforeDomain.YES,
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "All sorts",
          role = "General dog's body",
          details = "A labourer in varied industries",
        ),
      ),
    )
    val workInterests = aValidFutureWorkInterests(
      interests = listOf(
        aValidWorkInterest(
          workType = WorkInterestTypeDomain.OTHER,
          workTypeOther = "Varied interests",
          role = "Labourer",
        ),
      ),
    )
    val expectedResponse = aValidPreviousWorkResponse(
      id = workExperiences.reference,
      hasWorkedBefore = HasWorkedBefore.YES,
      typeOfWorkExperience = setOf(WorkType.OTHER),
      typeOfWorkExperienceOther = "All sorts",
      workExperience = setOf(
        aValidWorkExperienceResource(
          typeOfWorkExperience = WorkType.OTHER,
          otherWork = "All sorts",
          role = "General dog's body",
          details = "A labourer in varied industries",
        ),
      ),
      workInterests = aValidWorkInterestsResponse(
        id = workInterests.reference,
        workInterests = setOf(WorkType.OTHER),
        workInterestsOther = "Varied interests",
        particularJobInterests = setOf(
          WorkInterestDetail(
            workInterest = WorkType.OTHER,
            role = "Labourer",
          ),
        ),
        modifiedBy = workInterests.lastUpdatedBy!!,
        modifiedDateTime = workInterests.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      ),
      modifiedDateTime = workExperiences.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      modifiedBy = "bjones_gen",
    )

    // When
    val actual = mapper.toPreviousWorkResponse(workExperiences, workInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to PreviousWorkResponse given no future work interests`() {
    // Given
    val workExperiences = aValidPreviousWorkExperiences(
      hasWorkedBefore = HasWorkedBeforeDomain.YES,
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "All sorts",
          role = "General dog's body",
          details = "A labourer in varied industries",
        ),
      ),
    )
    val workInterests: FutureWorkInterests? = null
    val expectedResponse = aValidPreviousWorkResponse(
      id = workExperiences.reference,
      hasWorkedBefore = HasWorkedBefore.YES,
      typeOfWorkExperience = setOf(WorkType.OTHER),
      typeOfWorkExperienceOther = "All sorts",
      workExperience = setOf(
        aValidWorkExperienceResource(
          typeOfWorkExperience = WorkType.OTHER,
          otherWork = "All sorts",
          role = "General dog's body",
          details = "A labourer in varied industries",
        ),
      ),
      workInterests = null,
      modifiedDateTime = workExperiences.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      modifiedBy = "bjones_gen",
    )

    // When
    val actual = mapper.toPreviousWorkResponse(workExperiences, workInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @ParameterizedTest
  @CsvSource(
    "YES,YES",
    "NO,NO",
    "NOT_RELEVANT,NOT_RELEVANT",
  )
  fun `should map to PreviousWorkResponse with all possible has worked before options`(
    domain: HasWorkedBeforeDomain,
    dto: HasWorkedBefore,
  ) {
    // Given
    val workExperiences = aValidPreviousWorkExperiences(
      hasWorkedBefore = domain,
      experiences = emptyList(),
    )
    val workInterests = aValidFutureWorkInterests(
      interests = emptyList(),
    )
    val expectedResponse = aValidPreviousWorkResponse(
      id = workExperiences.reference,
      hasWorkedBefore = dto,
      typeOfWorkExperience = emptySet(),
      typeOfWorkExperienceOther = null,
      workExperience = emptySet(),
      workInterests = aValidWorkInterestsResponse(
        id = workInterests.reference,
        workInterests = emptySet(),
        workInterestsOther = null,
        particularJobInterests = emptySet(),
        modifiedBy = workInterests.lastUpdatedBy!!,
        modifiedDateTime = workInterests.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      ),
      modifiedDateTime = workExperiences.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      modifiedBy = "bjones_gen",
    )

    // When
    val actual = mapper.toPreviousWorkResponse(workExperiences, workInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to null given no previous work experience`() {
    // Given
    val workExperiences: PreviousWorkExperiences? = null
    // workInterests is subordinate to workExperience and ignored when the latter is null
    val workInterests = aValidFutureWorkInterests()

    // When
    val actual = mapper.toPreviousWorkResponse(workExperiences, workInterests)

    // Then
    assertThat(actual).isNull()
  }

  @Test
  fun `should map to PreviousWorkResponse given empty collections`() {
    // Given
    val workExperiences = aValidPreviousWorkExperiences(
      hasWorkedBefore = HasWorkedBeforeDomain.YES,
      experiences = emptyList(),
    )
    val workInterests = aValidFutureWorkInterests(
      interests = emptyList(),
    )
    val expectedResponse = aValidPreviousWorkResponse(
      id = workExperiences.reference,
      hasWorkedBefore = HasWorkedBefore.YES,
      typeOfWorkExperience = emptySet(),
      typeOfWorkExperienceOther = null,
      workExperience = emptySet(),
      workInterests = aValidWorkInterestsResponse(
        id = workInterests.reference,
        workInterests = emptySet(),
        workInterestsOther = null,
        particularJobInterests = emptySet(),
        modifiedBy = workInterests.lastUpdatedBy!!,
        modifiedDateTime = workInterests.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      ),
      modifiedDateTime = workExperiences.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
      modifiedBy = "bjones_gen",
    )

    // When
    val actual = mapper.toPreviousWorkResponse(workExperiences, workInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to UpdatePreviousWorkExperiencesDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdatePreviousWorkRequest()
    val expectedDto = aValidUpdatePreviousWorkExperiencesDto(
      reference = request.id!!,
      hasWorkedBefore = HasWorkedBeforeDomain.YES,
      experiences = listOf(
        WorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "Scientist",
          role = "Lab Technician",
          details = "Cleaning test tubes",
        ),
      ),
      prisonId = "BXI",
    )

    // When
    val actual = mapper.toUpdatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expectedDto)
  }
}
