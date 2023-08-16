package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.servlet.RequestDispatcher.ERROR_MESSAGE
import jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanAlreadyExistsException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
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
   * Exception handler to return a 403 Forbidden ErrorResponse for a prohibited action (e.g. a business rule violation).
   */
  @ExceptionHandler(
    value = [
      ActionPlanAlreadyExistsException::class,
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
   * Exception handler to return a 404 Not Found ErrorResponse
   */
  @ExceptionHandler(
    value = [
      ActionPlanNotFoundException::class,
      GoalNotFoundException::class,
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
      violations.joinToString(" ") { it.message }
    } else {
      "Validation error"
    }
    request.setAttribute(ERROR_MESSAGE, errorMessage, SCOPE_REQUEST)
    return populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)
  }

  /**
   * Overrides the MethodArgumentNotValidException exception handler to return a 400 Bad Request ErrorResponse
   */
  override fun handleMethodArgumentNotValid(
    e: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    return populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)
  }

  /**
   * Overrides the HttpMessageNotReadableException exception handler to return a 400 Bad Request ErrorResponse
   */
  override fun handleHttpMessageNotReadable(
    e: HttpMessageNotReadableException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    return populateErrorResponseAndHandleExceptionInternal(e, BAD_REQUEST, request)
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR.value(),
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  private fun populateErrorResponseAndHandleExceptionInternal(
    exception: Exception,
    status: HttpStatus,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    request.setAttribute(ERROR_STATUS_CODE, status.value(), SCOPE_REQUEST)
    val body = errorAttributes.getErrorResponse(request)
    return handleExceptionInternal(exception, body, HttpHeaders(), status, request)
  }
}
