package uk.gov.justice.digital.hmpps.educationandworkplanapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

private val OBJECT_MAPPER: ObjectMapper =
  ObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

/**
 * Extension function to perform a deep copy on any object.
 * Uses Jackson to serialize then deserialize to create the new object instance.
 */
fun <T> T.deepCopy(): T {
  return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(this), this!!::class.java)
}
