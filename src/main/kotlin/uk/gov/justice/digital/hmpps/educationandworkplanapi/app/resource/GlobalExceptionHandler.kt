package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.ValidationException
import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.InvalidGoalException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
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

  @ExceptionHandler(ActionPlanNotFoundException::class)
  fun handleNotFoundException(e: ActionPlanNotFoundException): ResponseEntity<ErrorResponse> {
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

  @ExceptionHandler(InvalidGoalException::class)
  fun handleInvalidGoalException(e: InvalidGoalException): ResponseEntity<ErrorResponse> {
    log.info("Invalid goal: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
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
}