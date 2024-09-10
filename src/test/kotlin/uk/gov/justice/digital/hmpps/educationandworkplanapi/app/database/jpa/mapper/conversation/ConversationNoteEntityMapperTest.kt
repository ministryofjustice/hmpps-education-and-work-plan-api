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

  private val mapper = ConversationNoteEntityMapperImpl()

  @Test
  fun `should map from create DTO to entity`() {
    // Given
    val createConversationNoteDto = aValidCreateConversationNoteDto(
      prisonId = "BXI",
      content = "Chris engaged well during our meeting and has made some good progress",
    )
    val createdBy = "asmith_gen"
    val createdByDisplayName = "Alex Smith"

    val earliestCreatedAndUpdatedTime = Instant.now()

    val expected = aValidConversationNoteEntity(
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
      createdAt = Instant.MIN,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = Instant.MIN,
      updatedBy = "asmith_gen",
      updatedByDisplayName = "Alex Smith",
      // JPA managed ID field - expect this to be null, implying a new db entity
      id = null,
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createConversationNoteDto, createdBy, createdByDisplayName)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedIdFieldPopulated()
      .hasAReference()
      .wasCreatedAtOrAfter(earliestCreatedAndUpdatedTime)
      .wasUpdatedAtOrAfter(earliestCreatedAndUpdatedTime)
      .usingRecursiveComparison()
      .ignoringFields("reference", "createdAt", "updatedAt")
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
      createdByDisplayName = "Alex Smith",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )

    val expected = aValidConversationNote(
      reference = entity.reference!!,
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      lastUpdatedAtPrison = "MDI",
      createdAt = createdAt,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      lastUpdatedAt = updatedAt,
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
    )

    // When
    val actual = mapper.fromEntityToDomain(entity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
