package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aFullyPopulatedInduction(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnRelease = aValidWorkOnRelease(),
  previousQualifications: PreviousQualifications? = aValidPreviousQualifications(),
  previousTraining: PreviousTraining = aValidPreviousTraining(),
  previousWorkExperiences: PreviousWorkExperiences? = aValidPreviousWorkExperiences(),
  inPrisonInterests: InPrisonInterests? = aValidInPrisonInterests(),
  personalSkillsAndInterests: PersonalSkillsAndInterests? = aValidPersonalSkillsAndInterests(),
  futureWorkInterests: FutureWorkInterests? = aValidFutureWorkInterests(),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
  conductedBy: String? = "John Smith",
  conductedByRole: String? = "Peer Mentor",
  conductedAt: LocalDate? = LocalDate.now(),
  note: NoteDto? = aValidNoteDto(prisonNumber, noteType = NoteType.INDUCTION, entityType = EntityType.INDUCTION),
) = Induction(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedByDisplayName = lastUpdatedByDisplayName,
  lastUpdatedAt = lastUpdatedAt,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
  conductedBy = conductedBy,
  completedDate = conductedAt,
  conductedByRole = conductedByRole,
  note = note,
)
