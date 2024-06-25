package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import arrow.core.Either
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.Problem
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse

fun <L : Problem, R> Either<L, R>.toResponseEntity(
  customSuccessHandler: ((R) -> ResponseEntity<Any>)? = null,
  provideStatus: (L) -> HttpStatus,
): ResponseEntity<Any> = this.fold(
  {
    val status = provideStatus(it)
    ResponseEntity
      .status(status)
      .body(ErrorResponse(status = status.value(), userMessage = it.errorMessage))
  },
  { content ->
    val handler = customSuccessHandler ?: ::defaultRightHandler
    handler(content)
  },
)

private fun <T> defaultRightHandler(content: T): ResponseEntity<Any> = if (content is Unit || content == null) {
  ResponseEntity.noContent().build()
} else {
  ResponseEntity.ok(content)
}
