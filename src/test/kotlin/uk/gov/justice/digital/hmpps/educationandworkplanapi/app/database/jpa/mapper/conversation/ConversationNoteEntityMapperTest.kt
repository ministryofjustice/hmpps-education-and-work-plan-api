package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationNoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.aValidConversationNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.assertThat
import java.time.Instant
import java.util.UUID

class ConversationNoteEntityMapperTest {

  private val mapper = ConversationNoteEntityMapper()

  @Test
  fun `should map from create DTO to entity`() {
    // Given
    val createConversationNoteDto = aValidCreateConversationNoteDto(
      prisonId = "BXI",
      content = "Chris engaged well during our meeting and has made some good progress",
    )

    val expected = aValidConversationNoteEntity(
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createConversationNoteDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val createdAt = Instant.now()
    val updatedAt = Instant.now()

    val entity = aValidConversationNoteEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
      createdAt = createdAt,
      createdBy = "asmith_gen",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
    )

    val expected = aValidConversationNote(
      reference = entity.reference,
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      lastUpdatedAtPrison = "MDI",
      createdAt = createdAt,
      createdBy = "asmith_gen",
      lastUpdatedAt = updatedAt,
      lastUpdatedBy = "bjones_gen",
    )

    // When
    val actual = mapper.fromEntityToDomain(entity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
