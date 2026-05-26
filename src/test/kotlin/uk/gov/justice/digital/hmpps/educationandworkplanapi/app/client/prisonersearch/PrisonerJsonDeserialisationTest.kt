package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrisonerJsonDeserialisationTest {
  private val objectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  @Test
  fun `should deserialise prisoner with isIndeterminateSentence defaulted to false when field is missing`() {
    // Given
    val json = prisonerJson(excludeFields = setOf("indeterminateSentence"))

    // When
    val actual = objectMapper.readValue(json, Prisoner::class.java)

    // Then
    assertThat(actual.isIndeterminateSentence).isFalse()
  }

  @Test
  fun `should deserialise prisoner with isRecall defaulted to false when field is missing`() {
    // Given
    val json = prisonerJson(excludeFields = setOf("recall"))

    // When
    val actual = objectMapper.readValue(json, Prisoner::class.java)

    // Then
    assertThat(actual.isRecall).isFalse()
  }

  @Test
  fun `should deserialise prisoner honouring isIndeterminateSentence and isRecall when fields are present`() {
    // Given
    val json = """
      {
        "prisonerNumber": "A1234BC",
        "legalStatus": "SENTENCED",
        "releaseDate": "2045-03-01",
        "prisonId": "BXI",
        "indeterminateSentence": true,
        "recall": true,
        "lastName": "BARRY",
        "firstName": "JOHN",
        "dateOfBirth": "1970-01-01",
        "cellLocation": "A-2-015",
        "nonDtoReleaseDateType": "ARD",
        "receptionDate": "2022-05-09",
        "sentenceStartDate": "2022-05-09",
        "inOutStatus": "IN"
      }
    """.trimIndent()

    // When
    val actual = objectMapper.readValue(json, Prisoner::class.java)

    // Then
    assertThat(actual.isIndeterminateSentence).isTrue()
    assertThat(actual.isRecall).isTrue()
  }

  private fun prisonerJson(excludeFields: Set<String> = emptySet()): String {
    val allFields = mapOf(
      "prisonerNumber" to "\"A1234BC\"",
      "legalStatus" to "\"SENTENCED\"",
      "releaseDate" to "\"2045-03-01\"",
      "prisonId" to "\"BXI\"",
      "indeterminateSentence" to "false",
      "recall" to "false",
      "lastName" to "\"BARRY\"",
      "firstName" to "\"JOHN\"",
      "dateOfBirth" to "\"1970-01-01\"",
      "cellLocation" to "\"A-2-015\"",
      "nonDtoReleaseDateType" to "\"ARD\"",
      "receptionDate" to "\"2022-05-09\"",
      "sentenceStartDate" to "\"2022-05-09\"",
      "inOutStatus" to "\"IN\"",
    )
    val body = allFields
      .filterKeys { it !in excludeFields }
      .entries
      .joinToString(",\n  ") { (k, v) -> "\"$k\": $v" }
    return "{\n  $body\n}"
  }
}
