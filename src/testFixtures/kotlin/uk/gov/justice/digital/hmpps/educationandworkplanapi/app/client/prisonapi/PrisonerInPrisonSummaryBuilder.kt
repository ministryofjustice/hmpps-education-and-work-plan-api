package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonPeriod
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.SignificantMovement
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.TransferDetail
import java.time.OffsetDateTime

/**
 * Constructs a [PrisonerInPrisonSummary] based on the following response from the prison-api:
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
  prisonerNumber: String = aValidPrisonNumber(),
  prisonPeriod: List<PrisonPeriod>? = listOf(
    aValidPrisonPeriod(),
    anotherValidPrisonPeriod(),
  ),
): PrisonerInPrisonSummary =
  PrisonerInPrisonSummary(prisonerNumber, prisonPeriod)

fun aValidPrisonPeriod(
  entryDate: OffsetDateTime = OffsetDateTime.now().minusMonths(6),
  movementDates: List<SignificantMovement> = listOf(
    aValidSignificantMovementAdmission(), // into BMI
    aValidSignificantMovementRelease(), // from MDI
    aValidSignificantMovementAdmissionAndRelease(), // in and out of BXI
  ),
  transfers: List<TransferDetail> = listOf(
    aValidTransferDetail(), // BMI to MDI
    anotherValidTransferDetail(), // MDI to BXI
  ),
  prisons: List<String> = listOf("BMI", "MDI", "BXI"),
  bookNumber: String? = "1234A",
  bookingId: Long = 1,
  releaseDate: OffsetDateTime? = null,
): PrisonPeriod =
  PrisonPeriod(
    entryDate = entryDate,
    movementDates = movementDates,
    transfers = transfers,
    prisons = prisons,
    bookNumber = bookNumber,
    bookingId = bookingId,
    releaseDate = releaseDate,
  )

fun anotherValidPrisonPeriod(
  entryDate: OffsetDateTime = OffsetDateTime.now().minusMonths(1),
  movementDates: List<SignificantMovement> = listOf(
    aValidSignificantMovementAdmissionAndRelease(), // in and out of BXI
  ),
  transfers: List<TransferDetail> = emptyList(), // according to the swagger spec, this will never be null
  prisons: List<String> = listOf("BXI"),
  bookNumber: String? = "5678B",
  bookingId: Long = 2,
  releaseDate: OffsetDateTime? = null,
): PrisonPeriod =
  PrisonPeriod(
    entryDate = entryDate,
    movementDates = movementDates,
    transfers = transfers,
    prisons = prisons,
    bookNumber = bookNumber,
    bookingId = bookingId,
    releaseDate = releaseDate,
  )

fun aValidSignificantMovementAdmission(
  reasonInToPrison: String = "Imprisonment Without Option",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.ADM,
  admittedIntoPrisonId: String = "BMI",
  dateInToPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(6),
  reasonOutOfPrison: String? = "Wedding/Civil Ceremony",
  dateOutOfPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(4),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.TAP,
  releaseFromPrisonId: String? = "MDI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison,
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison,
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidSignificantMovementRelease(
  reasonInToPrison: String = "Wedding/Civil Ceremony",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.TAP,
  admittedIntoPrisonId: String = "MDI",
  dateInToPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(4),
  reasonOutOfPrison: String? = "Conditional Release (CJA91) -SH Term>1YR",
  dateOutOfPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(2),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.REL,
  releaseFromPrisonId: String? = "BXI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison,
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison,
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidSignificantMovementAdmissionAndRelease(
  reasonInToPrison: String = "Recall From Intermittent Custody",
  inwardType: SignificantMovement.InwardType = SignificantMovement.InwardType.ADM,
  admittedIntoPrisonId: String = "BXI",
  dateInToPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(1),
  reasonOutOfPrison: String? = "Conditional Release (CJA91) -SH Term>1YR",
  dateOutOfPrison: OffsetDateTime? = OffsetDateTime.now(),
  outwardType: SignificantMovement.OutwardType? = SignificantMovement.OutwardType.REL,
  releaseFromPrisonId: String? = "BXI",
): SignificantMovement = SignificantMovement(
  reasonInToPrison = reasonInToPrison,
  inwardType = inwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  dateInToPrison = dateInToPrison,
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison,
  outwardType = outwardType,
  releaseFromPrisonId = releaseFromPrisonId,
)

fun aValidTransferDetail(
  dateOutOfPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(5),
  dateInToPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(5),
  transferReason: String? = "Compassionate Transfer",
  fromPrisonId: String? = "BMI",
  toPrisonId: String = "MDI",
): TransferDetail = TransferDetail(
  dateOutOfPrison = dateOutOfPrison,
  dateInToPrison = dateInToPrison,
  transferReason = transferReason,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun anotherValidTransferDetail(
  dateOutOfPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(1),
  dateInToPrison: OffsetDateTime? = OffsetDateTime.now().minusMonths(1),
  transferReason: String? = "Appeals",
  fromPrisonId: String? = "MDI",
  toPrisonId: String = "BXI",
): TransferDetail = TransferDetail(
  dateOutOfPrison = dateOutOfPrison,
  dateInToPrison = dateInToPrison,
  transferReason = transferReason,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)
