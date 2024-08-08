package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

/**
 * Jackson [JsonDeserializer] that deserializes an object property into a string.
 *
 * EG. Given the following JSON where the `additionalInformation` property is an object:
 * ```
 *         {
 *           "description": "A prisoner has been received into prison",
 *           "version": "1.0",
 *           "additionalInformation": { "nomsNumber": "A6099EA", "reason": "ADMISSION", "details": "ACTIVE IN:ADM-N", "currentLocation": "IN_PRISON", "prisonId": "SWI", "nomisMovementReasonCode": "N", "currentPrisonStatus": "UNDER_PRISON_CARE" }
 *         }
 * ```
 * The `additionalInformation` can be deserialized into a simple string property as follows:
 * ```
 * data class SomeType(
 *   @JsonDeserialize(using = RawJsonDeserializer::class) val additionalInformation: String,
 *   val description: String,
 *   val version: String,
 * )
 * ```
 *
 */
class RawJsonDeserializer : JsonDeserializer<String>() {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): String {
    val mapper = jp.codec as ObjectMapper
    val node = mapper.readTree<JsonNode>(jp)
    return mapper.writeValueAsString(node)
  }
}
