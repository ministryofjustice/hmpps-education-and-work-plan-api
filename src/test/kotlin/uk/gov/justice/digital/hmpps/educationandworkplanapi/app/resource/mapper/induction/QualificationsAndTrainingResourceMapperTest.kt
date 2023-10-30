package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsResponse
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel as HighestEducationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel as HighestEducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType as TrainingTypeApi

class QualificationsAndTrainingResourceMapperTest {
  private val mapper = QualificationsAndTrainingResourceMapperImpl()

  @Test
  fun `should map to PreviousQualificationsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateEducationAndQualificationsRequest()
    val expectedQualifications = listOf(
      Qualification(
        subject = "English",
        level = QualificationLevel.LEVEL_3,
        grade = "A",
      ),
      Qualification(
        subject = "Maths",
        level = QualificationLevel.LEVEL_3,
        grade = "B",
      ),
    )

    // When
    val actual = mapper.toCreatePreviousQualificationsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.educationLevel).isEqualTo(HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(actual.qualifications).isEqualTo(expectedQualifications)
  }

  @Test
  fun `should map to PreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateEducationAndQualificationsRequest()
    val expectedTrainingTypes = listOf(TrainingTypeDomain.CSCS_CARD, TrainingTypeDomain.OTHER)

    // When
    val actual = mapper.toCreatePreviousTrainingDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.trainingTypes).isEqualTo(expectedTrainingTypes)
    assertThat(actual.trainingTypeOther).isEqualTo("Any training")
  }

  @Test
  fun `should map to EducationAndQualificationResponse`() {
    // Given
    val qualifications = aValidPreviousQualifications(
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevel.LEVEL_1,
          grade = "C",
        ),
      ),
    )
    val training = aValidPreviousTraining(
      trainingTypes = listOf(TrainingTypeDomain.CSCS_CARD),
      trainingTypeOther = null,
    )
    val expectedResponse = aValidEducationAndQualificationsResponse(
      id = training.reference,
      educationLevel = HighestEducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = setOf(
        aValidAchievedQualification(
          subject = "English",
          level = AchievedQualification.Level.LEVEL_1,
          grade = "C",
        ),
      ),
      additionalTraining = setOf(TrainingTypeApi.CSCS_CARD),
      additionalTrainingOther = null,
      modifiedBy = "bjones_gen",
      modifiedDateTime = training.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
    )

    // When
    val actual = mapper.toEducationAndQualificationResponse(qualifications, training)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to EducationAndQualificationResponse when prisoner has no previous qualifications`() {
    // Given
    val qualifications: PreviousQualifications? = null
    val training = aValidPreviousTraining(
      trainingTypes = listOf(TrainingTypeDomain.CSCS_CARD),
      trainingTypeOther = null,
    )
    val expectedResponse = aValidEducationAndQualificationsResponse(
      id = training.reference,
      educationLevel = null,
      qualifications = null,
      additionalTraining = setOf(TrainingTypeApi.CSCS_CARD),
      additionalTrainingOther = null,
      modifiedBy = "bjones_gen",
      modifiedDateTime = training.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
    )

    // When
    val actual = mapper.toEducationAndQualificationResponse(qualifications, training)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to null when prisoner has no previous training`() {
    // Given
    val training: PreviousTraining? = null
    // qualifications are subordinate to training and ignored when the latter is null
    val qualifications = aValidPreviousQualifications(
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevel.LEVEL_1,
          grade = "C",
        ),
      ),
    )

    // When
    val actual = mapper.toEducationAndQualificationResponse(qualifications, training)

    // Then
    assertThat(actual).isNull()
  }

  @Test
  fun `should map to EducationAndQualificationResponse given empty collections`() {
    // Given
    val qualifications = aValidPreviousQualifications(
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = emptyList(),
    )
    val training = aValidPreviousTraining(
      trainingTypes = emptyList(),
      trainingTypeOther = null,
    )
    val expectedResponse = aValidEducationAndQualificationsResponse(
      id = training.reference,
      educationLevel = HighestEducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = emptySet(),
      additionalTraining = emptySet(),
      additionalTrainingOther = null,
      modifiedBy = "bjones_gen",
      modifiedDateTime = training.lastUpdatedAt!!.atOffset(ZoneOffset.UTC),
    )

    // When
    val actual = mapper.toEducationAndQualificationResponse(qualifications, training)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }
}
