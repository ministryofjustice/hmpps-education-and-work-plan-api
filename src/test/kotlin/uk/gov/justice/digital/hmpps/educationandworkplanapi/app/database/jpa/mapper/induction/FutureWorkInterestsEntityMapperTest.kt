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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat

@ExtendWith(MockitoExtension::class)
class FutureWorkInterestsEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: FutureWorkInterestsEntityMapper

  @Mock
  private lateinit var workInterestEntityMapper: WorkInterestEntityMapper

  @Mock
  private lateinit var entityListManager: InductionEntityListManager<WorkInterestEntity, WorkInterest>

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createWorkInterestsDto = aValidCreateFutureWorkInterestsDto()
    val expectedWorkInterestEntity = aValidWorkInterestEntity()
    val expected = aValidFutureWorkInterestsEntity(
      interests = mutableListOf(expectedWorkInterestEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(workInterestEntityMapper.fromDomainToEntity(any())).willReturn(expectedWorkInterestEntity)

    // When
    val actual = mapper.fromCreateDtoToEntity(createWorkInterestsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(workInterestEntityMapper).fromDomainToEntity(createWorkInterestsDto.interests[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val futureWorkInterestsEntity = aValidFutureWorkInterestsEntityWithJpaFieldsPopulated()
    val expectedWorkInterest = aValidWorkInterest()
    val expectedFutureWorkInterests = aValidFutureWorkInterests(
      reference = futureWorkInterestsEntity.reference,
      interests = listOf(expectedWorkInterest),
      createdAt = futureWorkInterestsEntity.createdAt!!,
      createdAtPrison = futureWorkInterestsEntity.createdAtPrison,
      createdBy = futureWorkInterestsEntity.createdBy!!,
      lastUpdatedAt = futureWorkInterestsEntity.updatedAt!!,
      lastUpdatedAtPrison = futureWorkInterestsEntity.updatedAtPrison,
      lastUpdatedBy = futureWorkInterestsEntity.updatedBy!!,
    )
    given(workInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedWorkInterest)

    // When
    val actual = mapper.fromEntityToDomain(futureWorkInterestsEntity)

    // Then
    assertThat(actual).isEqualTo(expectedFutureWorkInterests)
    verify(workInterestEntityMapper).fromEntityToDomain(futureWorkInterestsEntity.interests[0])
  }
}
