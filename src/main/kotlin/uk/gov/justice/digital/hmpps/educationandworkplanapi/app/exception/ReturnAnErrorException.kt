package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.exception

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse

class ReturnAnErrorException(val errorResponse: ErrorResponse) : Exception()
