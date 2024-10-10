package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType as DomainNoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType as ApiNoteType

@ExtendWith(MockitoExtension::class)
class NoteResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: NoteResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map domain Note to NoteResponse`() {
    // Given
    val noteReference = UUID.randomUUID()
    val createdAt = Instant.now().minus(1, ChronoUnit.HOURS)
    val updatedAt = Instant.now()

    val note = aValidNoteDto(
      reference = noteReference,
      content = "Note content",
      noteType = DomainNoteType.GOAL,
      createdBy = "ASMITH_GEN",
      createdAt = createdAt,
      createdAtPrison = "BXI",
      lastUpdatedBy = "BJONES_GEN",
      lastUpdatedAt = updatedAt,
      lastUpdatedAtPrison = "MDI",
    )

    val expectedCreatedAt = createdAt.atOffset(UTC)
    val expectedUpdatedAt = updatedAt.atOffset(UTC)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedCreatedAt, expectedUpdatedAt)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("ASMITH_GEN", true, "Alex Smith"),
      UserDetailsDto("BJONES_GEN", true, "Barry Jones"),
    )

    val expectedNoteResponse = aValidNoteResponse(
      reference = noteReference,
      content = "Note content",
      type = ApiNoteType.GOAL,
      createdBy = "ASMITH_GEN",
      createdByDisplayName = "Alex Smith",
      createdAt = expectedCreatedAt,
      createdAtPrison = "BXI",
      updatedBy = "BJONES_GEN",
      updatedByDisplayName = "Barry Jones",
      updatedAt = expectedUpdatedAt,
      updatedAtPrison = "MDI",
    )

    // When
    val actual = mapper.fromDomainToModel(note)

    // Then
    assertThat(actual).isEqualTo(expectedNoteResponse)
    verify(userService).getUserDetails("ASMITH_GEN")
    verify(userService).getUserDetails("BJONES_GEN")
    verify(instantMapper).toOffsetDateTime(createdAt)
    verify(instantMapper).toOffsetDateTime(updatedAt)
  }
}
