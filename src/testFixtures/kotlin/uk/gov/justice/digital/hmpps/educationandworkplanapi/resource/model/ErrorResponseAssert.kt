package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractAssert

fun assertThat(actual: ErrorResponse?) = ErrorResponseAssert(actual)

class ErrorResponseAssert(actual: ErrorResponse?) : AbstractAssert<ErrorResponseAssert, ErrorResponse?>(actual, ErrorResponseAssert::class.java) {

  fun hasStatus(expected: Int): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected status $expected, but was $status")
      }
    }
    return this
  }

  fun hasErrorCode(expected: String): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (errorCode != expected) {
        failWithMessage("Expected errorCode $expected, but was $errorCode")
      }
    }
    return this
  }

  fun hasUserMessage(expected: String): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (userMessage != expected) {
        failWithMessage("Expected userMessage $expected, but was $userMessage")
      }
    }
    return this
  }

  fun hasUserMessageContaining(expected: String): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (userMessage == null || !userMessage!!.contains(expected)) {
        failWithMessage("Expected message to contain $expected, but was $userMessage")
      }
    }
    return this
  }

  fun hasDeveloperMessage(expected: String): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (developerMessage != expected) {
        failWithMessage("Expected developerMessage $expected, but was $developerMessage")
      }
    }
    return this
  }

  fun hasDeveloperMessageContaining(expected: String): ErrorResponseAssert {
    isNotNull
    with(actual!!) {
      if (developerMessage == null || !developerMessage!!.contains(expected)) {
        failWithMessage("Expected message to contain $expected, but was $developerMessage")
      }
    }
    return this
  }
}
