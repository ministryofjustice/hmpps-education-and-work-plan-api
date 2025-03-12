package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonPeriod
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.SignificantMovement
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.TransferDetail
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Constructs a [PrisonerInPrisonSummary] based on the following response from the prison-api (ignoring dates):
 * ```
 *   {
 *    "prisonerNumber":"A1234BC",
 *    "prisonPeriod":[
 *       {
 *          "entryDate":"2023-07-15T15:17:00.911592Z",
 *          "movementDates":[
 *             {
 *                "reasonInToPrison":"Imprisonment Without Option",
 *                "inwardType":"ADM",
 *                "admittedIntoPrisonId":"BMI",
 *                "dateInToPrison":"2023-07-15T15:17:00.912959Z",
 *                "reasonOutOfPrison":"Wedding/Civil Ceremony",
 *                "dateOutOfPrison":"2023-09-15T15:17:00.912969Z",
 *                "outwardType":"TAP",
 *                "releaseFromPrisonId":"MDI"
 *             },
 *             {
 *                "reasonInToPrison":"Wedding/Civil Ceremony",
 *                "inwardType":"TAP",
 *                "admittedIntoPrisonId":"MDI",
 *                "dateInToPrison":"2023-09-15T15:17:00.913499Z",
 *                "reasonOutOfPrison":"Conditional Release (CJA91) -SH Term>1YR",
 *                "dateOutOfPrison":"2023-11-15T15:17:00.91351Z",
 *                "outwardType":"REL",
 *                "releaseFromPrisonId":"BXI"
 *             },
 *             {
 *                "reasonInToPrison":"Recall From Intermittent Custody",
 *                "inwardType":"ADM",
 *                "admittedIntoPrisonId":"BXI",
 *                "dateInToPrison":"2023-12-15T15:17:00.913518Z",
 *                "reasonOutOfPrison":"Conditional Release (CJA91) -SH Term>1YR",
 *                "dateOutOfPrison":"2024-01-15T15:17:00.913527Z",
 *                "outwardType":"REL",
 *                "releaseFromPrisonId":"BXI"
 *             }
 *          ],
 *          "transfers":[
 *             {
 *                "toPrisonId":"MDI",
 *                "dateOutOfPrison":"2023-08-15T15:17:00.990436Z",
 *                "dateInToPrison":"2023-08-15T15:17:00.99046Z",
 *                "transferReason":"Compassionate Transfer",
 *                "fromPrisonId":"BMI"
 *             },
 *             {
 *                "toPrisonId":"BXI",
 *                "dateOutOfPrison":"2023-12-15T15:17:00.990568Z",
 *                "dateInToPrison":"2023-12-15T15:17:00.990573Z",
 *                "transferReason":"Appeals",
 *                "fromPrisonId":"MDI"
 *             }
 *          ],
 *          "prisons":[
 *             "BMI",
 *             "MDI",
 *             "BXI"
 *          ],
 *          "bookNumber":"1234A",
 *          "bookingId":1
 *       },
 *       {
 *          "entryDate":"2023-12-15T15:17:00.990649Z",
 *          "movementDates":[
 *             {
 *                "reasonInToPrison":"Recall From Intermittent Custody",
 *                "inwardType":"ADM",
 *                "admittedIntoPrisonId":"BXI",
 *                "dateInToPrison":"2023-12-15T15:17:00.990654Z",
 *                "reasonOutOfPrison":"Conditional Release (CJA91) -SH Term>1YR",
 *                "dateOutOfPrison":"2024-01-15T15:17:00.990658Z",
 *                "outwardType":"REL",
 *                "releaseFromPrisonId":"BXI"
 *             }
 *          ],
 *          "transfers":[
 *          ],
 *          "prisons":[
 *             "BXI"
 *          ],
 *          "bookNumber":"5678B",
 *          "bookingId":2
 *       }
 *    ]
 * }
 * ```
 */
fun aValidPrisonerInPrisonSummary(
  prisonerNumber: String = randomValidPrisonNumber(),
  prisonPeriod: List<PrisonPeriod>? = listOf(
    aValidPrisonPeriod(),
    anotherValidPrisonPeriod(),
  ),
): PrisonerInPrisonSummary = PrisonerInPrisonSummary(prisonerNumber, prisonPeriod)

fun aValidPrisonPeriod(
  entryDate: LocalDateTime = LocalDateTime.now().minusMonths(6),
  movementDates: List<SignificantMovement> = listOf(
    // movement into BMI
    aValidSignificantMovementAdmission(),
    // movement from MDI
    aValidSignificantMovementRelease(),
    // admission and release in and out of BXI
    aValidSignificantMovementAdmissionAndRelease(),
  ),
  transfers: List<TransferDetail> = listOf(
    // BMI to MDI
    aValidTransferDetail(),
    // MDI to BXI
    anotherValidTransferDetail(),
  ),
  prisons: List<String> = listOf("BMI", "MDI", "BXI"),
  bookNumber: String = "1234A",
  bookingId: Long = 1,
  releaseDate: LocalDateTime? = null,
): PrisonPeriod =
  PrisonPeriod(
    entryDate = entryDate.asIsoDateTimeString()!!,
    movementDates = movementDates,
    transfers = transfers,
    prisons = prisons,
    bookNumber = bookNumber,
    bookingId = bookingId,
    releaseDate = releaseDate.asIsoDateTimeString(),
  )

fun anotherValidPrisonPeriod(
  entryDate: LocalDateTime = LocalDateTime.now().minusMonths(1),
  movementDates: List<SignificantMovement> = listOf(
    // An admission and release in and out of BXI
    aValidSignificantMovementAdmissionAndRelease(),
  ),
  // as per the swagger spec, transfers will never be null
  transfers: List<TransferDetail> = emptyList(),
  prisons: List<String> = listOf("BXI"),
  bookNumber: String = "5678B",
  bookingId: Long = 2,
  releaseDate: LocalDateTime? = null,
): PrisonPeriod =
  PrisonPeriod(
    entryDate = entryDate.asIsoDateTimeString()!!,
    movementDates = movementDates,
    transfers = transfers,
    prisons = prisons,
    bookNumber = bookNumber,
    bookingId = bookingId,
    releaseDate = releaseDate.asIsoDateTimeString(),
  )

fun aValidSignificantMovementAdmission(
  reasonInToPrison: String = "Imprisonment Without Option",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.ADM,
  admittedIntoPrisonId: String = "BMI",
  dateInToPrison: LocalDateTime? = LocalDateTime.now().minusMonths(6),
  reasonOutOfPrison: String? = "Wedding/Civil Ceremony",
  dateOutOfPrison: LocalDateTime? = LocalDateTime.now().minusMonths(4),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.TAP,
  releaseFromPrisonId: String? = "MDI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison.asIsoDateTimeString(),
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison.asIsoDateTimeString(),
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidSignificantMovementRelease(
  reasonInToPrison: String = "Wedding/Civil Ceremony",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.TAP,
  admittedIntoPrisonId: String = "MDI",
  dateInToPrison: LocalDateTime? = LocalDateTime.now().minusMonths(4),
  reasonOutOfPrison: String? = "Conditional Release (CJA91) -SH Term>1YR",
  dateOutOfPrison: LocalDateTime? = LocalDateTime.now().minusMonths(3),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.REL,
  releaseFromPrisonId: String? = "BXI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison.asIsoDateTimeString(),
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison.asIsoDateTimeString(),
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidSignificantMovementAdmissionAndRelease(
  reasonInToPrison: String = "Recall From Intermittent Custody",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.ADM,
  admittedIntoPrisonId: String = "BXI",
  dateInToPrison: LocalDateTime? = LocalDateTime.now().minusMonths(2),
  reasonOutOfPrison: String? = "Conditional Release (CJA91) -SH Term>1YR",
  dateOutOfPrison: LocalDateTime? = LocalDateTime.now().minusMonths(1),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.REL,
  releaseFromPrisonId: String? = "BXI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison.asIsoDateTimeString(),
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison.asIsoDateTimeString(),
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidTransferDetail(
  dateOutOfPrison: LocalDateTime? = LocalDateTime.now().minusMonths(5),
  dateInToPrison: LocalDateTime? = LocalDateTime.now().minusMonths(5),
  transferReason: String? = "Compassionate Transfer",
  fromPrisonId: String? = "BMI",
  toPrisonId: String = "MDI",
): TransferDetail = TransferDetail(
  dateOutOfPrison = dateOutOfPrison.asIsoDateTimeString(),
  dateInToPrison = dateInToPrison.asIsoDateTimeString(),
  transferReason = transferReason,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun anotherValidTransferDetail(
  dateOutOfPrison: LocalDateTime? = LocalDateTime.now().minusMonths(4),
  dateInToPrison: LocalDateTime? = LocalDateTime.now().minusMonths(4),
  transferReason: String? = "Appeals",
  fromPrisonId: String? = "MDI",
  toPrisonId: String = "BXI",
): TransferDetail = TransferDetail(
  dateOutOfPrison = dateOutOfPrison.asIsoDateTimeString(),
  dateInToPrison = dateInToPrison.asIsoDateTimeString(),
  transferReason = transferReason,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

private fun LocalDateTime?.asIsoDateTimeString() = this?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
