package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Represents a Prisoner's Induction, which is typically carried out by a CIAG officer shortly after the Prisoner
 * has entered a Prison (either when starting a new sentence, or after being transferred from another Prison). Once
 * established, an Induction can be reviewed and updated periodically, depending on the practices of the Prison and
 * the needs of the Prisoner.
 */
data class Induction(
  /**
   * A unique reference for the Induction that can be referred to outside of this application.
   */
  val reference: UUID,
  /**
   * The identifier of the Prisoner (confusingly called "prison" number throughout HMPPS).
   */
  val prisonNumber: String,
  /**
   * Details of the Prisoner's work aspirations. Mandatory as it is always asked as part of an Induction.
   */
  val workOnRelease: WorkOnRelease,
  /**
   * Qualifications that the Prisoner may have achieved previously. Null if the Prisoner has not been asked about their
   * educational history.
   */
  val previousQualifications: PreviousQualifications?,
  /**
   * Any additional training that the Prisoner may have done previously. Mandatory as the Prisoner is always asked about
   * it.
   */
  val previousTraining: PreviousTraining,
  /**
   * Details of any previous work experience that the Prisoner may have had. Null if the Prisoner has not been asked
   * about their work history.
   */
  val previousWorkExperiences: PreviousWorkExperiences?,
  /**
   * Work or training interests that the Prisoner wishes to undertake during their time in prison. Null if the Prisoner
   * has not been asked about their in-prison interests.
   */
  val inPrisonInterests: InPrisonInterests?,
  /**
   * Any personal skills and interests that the Prisoner has. Null if the Prisoner has not been asked about their
   * skills and interests.
   */
  val personalSkillsAndInterests: PersonalSkillsAndInterests?,
  /**
   * Any future (post release) work interests that the Prisoner has. Null if the Prisoner has not been asked about their
   * future work interests.
   */
  val futureWorkInterests: FutureWorkInterests?,
  /**
   * The user ID of the person (logged-in user) who created the Induction.
   */
  val createdBy: String?,
  /**
   * The timestamp when this Induction was created.
   */
  val createdAt: Instant?,
  /**
   * The ID of the Prison that the Prisoner was at when this induction was created.
   */
  val createdAtPrison: String,
  /**
   * The user ID of the person (logged-in user) who updated the Induction.
   */
  val lastUpdatedBy: String?,
  /**
   * The timestamp when this Induction was updated.
   */
  val lastUpdatedAt: Instant?,
  /**
   * The ID of the Prison that the Prisoner was at when this induction was updated.
   */
  val lastUpdatedAtPrison: String,

  /**
   * the person who conducted the induction if it were not the actual user
   */
  val conductedBy: String?,

  /**
   * the role of the person who conducted the induction if it were not the actual user
   */
  val conductedByRole: String?,

  /**
   * The date the induction was carried out
   */
  val completedDate: LocalDate?,

  val note: NoteDto?,
)
