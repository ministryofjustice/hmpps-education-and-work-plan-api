package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateFutureWorkInterestsDto

@ExtendWith(MockitoExtension::class)
class FutureWorkInterestsEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: FutureWorkInterestsEntityMapperImpl

  @Mock
  private lateinit var workInterestEntityMapper: WorkInterestEntityMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createWorkInterestsDto = aValidCreateFutureWorkInterestsDto()
    val expectedWorkInterestEntity = aValidWorkInterestEntity()
    val expected = aValidFutureWorkInterestsEntity(
      interests = listOf(expectedWorkInterestEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(workInterestEntityMapper.fromDomainToEntity(any())).willReturn(expectedWorkInterestEntity)

    // When
    val actual = mapper.fromDtoToEntity(createWorkInterestsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(workInterestEntityMapper).fromDomainToEntity(createWorkInterestsDto.interests[0])
  }
}
