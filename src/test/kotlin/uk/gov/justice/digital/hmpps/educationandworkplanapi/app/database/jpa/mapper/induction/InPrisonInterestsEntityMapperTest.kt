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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInPrisonInterestsDto

@ExtendWith(MockitoExtension::class)
class InPrisonInterestsEntityMapperTest {

  @InjectMocks
  private val mapper = InPrisonInterestsEntityMapperImpl()

  @Mock
  private lateinit var workInterestEntityMapper: InPrisonWorkInterestEntityMapper

  @Mock
  private lateinit var trainingInterestEntityMapper: InPrisonTrainingInterestEntityMapper

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
      .hasAReference()
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
      reference = inPrisonInterestsEntity.reference!!,
      inPrisonWorkInterests = mutableListOf(expectedWorkInterest),
      inPrisonTrainingInterests = mutableListOf(expectedTrainingInterest),
      createdAt = inPrisonInterestsEntity.createdAt!!,
      createdAtPrison = inPrisonInterestsEntity.createdAtPrison!!,
      createdBy = inPrisonInterestsEntity.createdBy!!,
      createdByDisplayName = inPrisonInterestsEntity.createdByDisplayName!!,
      lastUpdatedAt = inPrisonInterestsEntity.updatedAt!!,
      lastUpdatedAtPrison = inPrisonInterestsEntity.updatedAtPrison!!,
      lastUpdatedBy = inPrisonInterestsEntity.updatedBy!!,
      lastUpdatedByDisplayName = inPrisonInterestsEntity.updatedByDisplayName!!,
    )

    given(workInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedWorkInterest)
    given(trainingInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedTrainingInterest)

    // When
    val actual = mapper.fromEntityToDomain(inPrisonInterestsEntity)

    // Then
    assertThat(actual).isEqualTo(expectedInPrisonInterests)
    verify(workInterestEntityMapper).fromEntityToDomain(inPrisonInterestsEntity.inPrisonWorkInterests!![0])
    verify(trainingInterestEntityMapper).fromEntityToDomain(inPrisonInterestsEntity.inPrisonTrainingInterests!![0])
  }
}
