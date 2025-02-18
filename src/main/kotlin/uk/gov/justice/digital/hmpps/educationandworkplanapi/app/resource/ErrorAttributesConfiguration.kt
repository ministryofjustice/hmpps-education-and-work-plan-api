package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.FieldError
import org.springframework.web.context.request.WebRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

@Configuration
class ErrorAttributesConfiguration {

  @Bean
  fun apiRequestErrorAttributes(messageSource: MessageSource) = ApiRequestErrorAttributes(messageSource)
}

class ApiRequestErrorAttributes(
  private val messageSource: MessageSource,
) : DefaultErrorAttributes() {

  companion object {
    private const val TIMESTAMP = "timestamp"
    private const val STATUS = "status"
    private const val ERROR = "error"
    private const val MESSAGE = "message"
    private const val ERRORS = "errors"

    private val errorAttributeOptions = ErrorAttributeOptions.defaults()
      .including(Include.MESSAGE, Include.BINDING_ERRORS)
  }

  fun getErrorResponse(request: WebRequest): ErrorResponse = getErrorAttributes(request, errorAttributeOptions).let {
    ErrorResponse(
      status = it.getStatus(),
      errorCode = it.getError(),
      userMessage = it.getMessage(),
      developerMessage = it.getErrors().toString(),
    )
  }

  private fun Map<String, Any>.getTimeStamp(): OffsetDateTime = (this[TIMESTAMP] as Date).toInstant().atOffset(ZoneOffset.UTC)

  private fun Map<String, Any>.getStatus(): Int = this[STATUS] as Int

  private fun Map<String, Any>.getError(): String = this[ERROR].toString()

  private fun Map<String, Any>.getMessage(): String = this[MESSAGE].toString()

  private fun Map<String, Any>.getErrors(): List<String>? = (this[ERRORS] as List<*>?)
    ?.map { it as FieldError }
    ?.map {
      with(it) {
        val message = messageSource.getMessage(this, Locale.getDefault())
        "Error on field '$field': rejected value [$rejectedValue], $message"
      }
    }
}
