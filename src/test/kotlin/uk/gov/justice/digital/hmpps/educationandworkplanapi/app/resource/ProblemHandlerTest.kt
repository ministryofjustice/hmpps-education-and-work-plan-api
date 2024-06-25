package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import arrow.core.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.Problem
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse

class ProblemHandlerTest {

  @Test
  fun `should return no content if result is right but null`() {
    val either: Either<Problem, String?> = Either.Right(null)
    val response = either.toResponseEntity { HttpStatus.NOT_FOUND }
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    assertThat(response.body).isNull()
  }

  @Test
  fun `should return no content if result is right with unit type`() {
    val either: Either<Problem, Unit> = Either.Right(Unit)
    val response = either.toResponseEntity { HttpStatus.NOT_FOUND }
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    assertThat(response.body).isNull()
  }

  @Test
  fun `should return ok with body if right`() {
    val either: Either<Problem, String?> = Either.Right("Foo")
    val response = either.toResponseEntity { HttpStatus.NOT_FOUND }
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.body).isEqualTo("Foo")
  }

  @Test
  fun `should return error entity with problem and matching response code`() {
    val either: Either<Problem, String?> = Either.Left(Problem("Bang!"))
    val response = either.toResponseEntity { HttpStatus.NOT_FOUND }
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(response.body).isEqualTo(ErrorResponse(status = 404, userMessage = "Bang!"))
  }

  @Test
  fun `should return ok with custom body mapping if right`() {
    val either: Either<Problem, String?> = Either.Right("Foo")
    val response = either.toResponseEntity({ ResponseEntity.accepted().build() }) { HttpStatus.NOT_FOUND }
    assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    assertThat(response.body).isNull()
  }
}
