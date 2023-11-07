package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkExperienceResource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkInterests
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType as WorkExperienceTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType as WorkInterestTypeDomain

class PreviousWorkExperiencesResourceMapperTest {
  private val mapper = PreviousWorkExperiencesResourceMapperImpl()

  @Test
  fun `should map to CreatePreviousWorkExperiencesDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousWorkRequest()
    val expectedExperiences = listOf(
      WorkExperience(
        experienceType = WorkExperienceTypeDomain.OTHER,
        experienceTypeOther = "Scientist",
        role = "Lab Technician",
        details = "Cleaning test tubes",
      ),
    )

    // When
    val actual = mapper.toCreatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.experiences).isEqualTo(expectedExperiences)
  }

  @Test
  fun `should map to PreviousWorkResponse`() {
    // Given
    val workExperiences = aValidPreviousWorkExperiences(
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
      hasWorkedBefore = true,
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
      workInterests = aValidWorkInterests(
        id = workInterests.reference,
        workInterests = setOf(WorkType.OTHER),
        workInterestsOther = "Varied interests",
        particularJobInterests = setOf(
          WorkInterestDetail(
            workInterest = WorkType.OTHER,
            role = "Labourer",
          ),
        ),
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
      hasWorkedBefore = true,
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
      experiences = emptyList(),
    )
    val workInterests = aValidFutureWorkInterests(
      interests = emptyList(),
    )
    val expectedResponse = aValidPreviousWorkResponse(
      id = workExperiences.reference,
      hasWorkedBefore = false,
      typeOfWorkExperience = null,
      typeOfWorkExperienceOther = null,
      workExperience = null,
      workInterests = aValidWorkInterests(
        id = workInterests.reference,
        workInterests = emptySet(),
        workInterestsOther = null,
        particularJobInterests = emptySet(),
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
    val expectedExperiences = listOf(
      WorkExperience(
        experienceType = WorkExperienceTypeDomain.OTHER,
        experienceTypeOther = "Scientist",
        role = "Lab Technician",
        details = "Cleaning test tubes",
      ),
    )

    // When
    val actual = mapper.toUpdatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.experiences).isEqualTo(expectedExperiences)
  }
}
