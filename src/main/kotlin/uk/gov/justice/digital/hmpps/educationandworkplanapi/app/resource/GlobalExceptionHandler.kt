package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.servlet.RequestDispatcher.ERROR_MESSAGE
import jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ElementKind
import mu.KotlinLogging
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InvalidInductionScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.InvalidGoalStateException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.NoArchiveReasonException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.PrisonerHasNoGoalsException
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.UpstreamResponseException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingSentenceStartDateAndReceptionDateException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse

private val log = KotlinLogging.logger {}

/**
 * Global Exception Handler. Handles specific exceptions thrown by the application by returning a suitable [ErrorResponse]
 * response entity.
 *
 * Our standard pattern here is to return an [ErrorResponse]. Please think carefully about writing a response handler
 * method that does not follow this pattern. Please try not to use [handleExceptionInternal] as this returns a response
 * body of a simple string rather than a structured response body.
 *
 * Our preferred approach is to use the method [populateErrorResponseAndHandleExceptionInternal] which builds and returns
 * the [ErrorResponse] complete with correctly populated status code field. This method also populates the message field
 * of [ErrorResponse] from the exception. In the case that the exception message is not suitable for exposing through
 * the REST API, this can be overridden by manually setting the message on the request attribute. eg:
 *
 * ```
 *     request.setAttribute(ERROR_MESSAGE, "A simpler error message that does not expose internal detail", SCOPE_REQUEST)
 * ```
 *
 */
@RestControllerAdvice
class GlobalExceptionHandler(
  private val errorAttributes: ApiRequestErrorAttributes,
) : ResponseEntityExceptionHandler() {

  /**
   * Exception handler to return a 403 Forbidden ErrorResponse for an AccessDeniedException.
   */
  @ExceptionHandler(
    value = [
      AccessDeniedException::class,
    ],
  )
  protected fun handleAccessDeniedExceptionReturnForbiddenErrorResponse(
    e: RuntimeException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    log.info("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN.value(),
          userMessage = e.message,
          developerMessage = "Access denied on ${request.getDescription(false)}",
        ),
      )
  }

  /**
   * Exception handler to return a 400 bad request when a goal is archived with no reason text.
   */
  @ExceptionHandler(
    value = [
      NoArchiveReasonException::class,
    ],
  )
  protected fun handleNoArchiveReasonException(
    e: RuntimeException,
    request: WebRequest,
  ): ResponseEntity<Any> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST.value(),
        userMessage = e.message,
      ),
    )

  @ExceptionHandler(value = [MissingSentenceStartDateAndReceptionDateException::class])
  protected fun handleMissingDateException(e: RuntimeException, request: WebRequest) = ResponseEntity
    .status(BAD_REQUEST)
    .body(ErrorResponse(status = BAD_REQUEST.value(), userMessage = e.message))

  /**
   * Exception handler to return a 403 Forbidden ErrorResponse for a prohibited action (e.g. a business rule violation).
   */
  @ExceptionHandler(
    value = [
      ActionPlanAlreadyExistsException::class,
      uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionAlreadyExistsException::class,
    ],
  )
  protected fun handleExceptionReturnForbiddenErrorResponse(
    e: RuntimeException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    log.info("Forbidden request: {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN.value(),
          userMessage = e.message,
        ),
      )
  }

  /**
   * Exception handler to return a 409 Conflict ErrorResponse.
   */
  @ExceptionHandler(
    value = [
      EducationAlreadyExistsException::class,
      InvalidGoalStateException::class,
      InvalidReviewScheduleStatusException::class,
      InvalidInductionScheduleStatusException::class,
      ReviewScheduleNoReleaseDateForSentenceTypeException::class,
    ],
  )
  protected fun handleExceptionReturnConflictErrorResponse(
    e: RuntimeException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    log.info("Conflict exception: {}", e.message)
    return ResponseEntity
      .status(CONFLICT)
      .body(
        ErrorResponse(
          status = CONFLICT.value(),
          userMessage = e.message,
        ),
      )
  }

  /**
   * Exception handler to return a 404 Not Found ErrorResponse
   */
  @ExceptionHandler(
    value = [
      ActionPlanNotFoundException::class,
      GoalNotFoundException::class,
      TimelineNotFoundException::class,
      InductionNotFoundException::class,
      EducationNotFoundException::class,
      PrisonerHasNoGoalsException::class,
      ReviewScheduleNotFoundException::class,
      InductionScheduleNotFoundException::class,
      PrisonerNotFoundException::class,
    ],
  )
  fun handleExceptionReturnNotFoundErrorResponse(
    e: RuntimeException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    log.info("Not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND.value(),
          userMessage = e.message,
        ),
      )
  }

  /**
   * Exception handler to return a 400 Bad Request ErrorResponse, specifically for a ConstraintViolationException.
   *
   * This is because the message property of ConstraintViolationException does not contain sufficient/formatted details
   * as to the nature of the constraint violations. This handler constructs the error message from each violation in the
   * exception, before using it to create the ErrorResponse which is rendered through the REST API.
   */
  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(
    e: ConstraintViolationException,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    val violations: Set<ConstraintViolation<*>> = e.constraintViolations
    val errorMessage = if (violations.isNotEmpty()) {
      violations.joinToString {
        if (it.relatesToNamedParameter()) {
          "${it.propertyPath} ${it.message}"
        } else {
          it.message
        }
      }
    } else {
      "Validation error"
    }
    request.setAttribute(ERROR_MESSAGE, errorMessage, SCOPE_REQUEST)
    return populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)
  }

  /**
   * Exception handler to return a 503 Service Unavailable ErrorResponse, specifically for a DataAccessException.
   */
  @ExceptionHandler(DataAccessException::class)
  fun handleDataAccessException(
    e: DataAccessException,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    log.error("Unexpected database exception", e)
    request.setAttribute(ERROR_MESSAGE, "Service unavailable", SCOPE_REQUEST)
    return populateErrorResponseAndHandleExceptionInternal(e, SERVICE_UNAVAILABLE, request)
  }

  @ExceptionHandler(UpstreamResponseException::class)
  fun handleUpstreamResponseException(
    e: UpstreamResponseException,
    request: WebRequest,
  ) = populateErrorResponseAndHandleExceptionInternal(e, HttpStatusCode.valueOf(e.statusCode), request)

  /**
   * Overrides the MethodArgumentNotValidException exception handler to return a 400 Bad Request ErrorResponse
   */
  override fun handleMethodArgumentNotValid(
    e: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? = populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)

  /**
   * Overrides the HttpMessageNotReadableException exception handler to return a 400 Bad Request ErrorResponse
   */
  override fun handleHttpMessageNotReadable(
    e: HttpMessageNotReadableException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? = populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)

  @ExceptionHandler(Exception::class)
  fun unexpectedExceptionHandler(e: Exception, request: WebRequest): ResponseEntity<Any>? {
    log.error("Unexpected exception", e)
    request.setAttribute(ERROR_MESSAGE, "Service unavailable", SCOPE_REQUEST)
    return populateErrorResponseAndHandleExceptionInternal(e, INTERNAL_SERVER_ERROR, request)
  }

  private fun populateErrorResponseAndHandleExceptionInternal(
    exception: Exception,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    request.setAttribute(ERROR_STATUS_CODE, status.value(), SCOPE_REQUEST)
    val body = errorAttributes.getErrorResponse(request)
    return handleExceptionInternal(exception, body, HttpHeaders(), status, request)
  }

  /**
   * Returns true is this [ConstraintViolation] relates to a named parameter such as a constraint annotation on a
   * property in the request body, or a constraint annotation on the method argument.
   * Knowing whether the constraint relates to a named parameter means we can use the name in the error response.
   */
  private fun ConstraintViolation<*>.relatesToNamedParameter(): Boolean = propertyPath is PathImpl &&
    when ((propertyPath as PathImpl).leafNode.kind) {
      ElementKind.PROPERTY, ElementKind.PARAMETER -> true
      else -> false
    }
}
