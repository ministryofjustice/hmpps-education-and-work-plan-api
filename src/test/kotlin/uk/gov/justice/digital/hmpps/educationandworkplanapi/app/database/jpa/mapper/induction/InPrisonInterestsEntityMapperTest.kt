package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat

@ExtendWith(MockitoExtension::class)
class InPrisonInterestsEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: InPrisonInterestsEntityMapper

  @Mock
  private lateinit var workInterestEntityMapper: InPrisonWorkInterestEntityMapper

  @Mock
  private lateinit var trainingInterestEntityMapper: InPrisonTrainingInterestEntityMapper

  @Mock
  private lateinit var workInterestEntityListManager: InductionEntityListManager<InPrisonWorkInterestEntity, InPrisonWorkInterest>

  @Mock
  private lateinit var trainingInterestEntityListManager: InductionEntityListManager<InPrisonTrainingInterestEntity, InPrisonTrainingInterest>

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createInPrisonInterestsDto = aValidCreateInPrisonInterestsDto()
    val expectedInPrisonWorkInterestEntity = aValidInPrisonWorkInterestEntity()
    val expectedInPrisonTrainingInterestEntity = aValidInPrisonTrainingInterestEntity()
    val expected = aValidInPrisonInterestsEntity(
      inPrisonWorkInterests = mutableListOf(expectedInPrisonWorkInterestEntity),
      inPrisonTrainingInterests = mutableListOf(expectedInPrisonTrainingInterestEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(workInterestEntityMapper.fromDomainToEntity(any())).willReturn(expectedInPrisonWorkInterestEntity)
    given(trainingInterestEntityMapper.fromDomainToEntity(any())).willReturn(
      expectedInPrisonTrainingInterestEntity,
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createInPrisonInterestsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(workInterestEntityMapper).fromDomainToEntity(createInPrisonInterestsDto.inPrisonWorkInterests[0])
    verify(trainingInterestEntityMapper).fromDomainToEntity(createInPrisonInterestsDto.inPrisonTrainingInterests[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val inPrisonInterestsEntity = aValidInPrisonInterestsEntityWithJpaFieldsPopulated()
    val expectedWorkInterest = aValidInPrisonWorkInterest()
    val expectedTrainingInterest = aValidInPrisonTrainingInterest()
    val expectedInPrisonInterests = aValidInPrisonInterests(
      reference = inPrisonInterestsEntity.reference,
      inPrisonWorkInterests = mutableListOf(expectedWorkInterest),
      inPrisonTrainingInterests = mutableListOf(expectedTrainingInterest),
      createdAt = inPrisonInterestsEntity.createdAt!!,
      createdAtPrison = inPrisonInterestsEntity.createdAtPrison,
      createdBy = inPrisonInterestsEntity.createdBy!!,
      lastUpdatedAt = inPrisonInterestsEntity.updatedAt!!,
      lastUpdatedAtPrison = inPrisonInterestsEntity.updatedAtPrison,
      lastUpdatedBy = inPrisonInterestsEntity.updatedBy!!,
    )

    given(workInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedWorkInterest)
    given(trainingInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedTrainingInterest)

    // When
    val actual = mapper.fromEntityToDomain(inPrisonInterestsEntity)

    // Then
    assertThat(actual).isEqualTo(expectedInPrisonInterests)
    verify(workInterestEntityMapper).fromEntityToDomain(inPrisonInterestsEntity.inPrisonWorkInterests[0])
    verify(trainingInterestEntityMapper).fromEntityToDomain(inPrisonInterestsEntity.inPrisonTrainingInterests[0])
  }
}
