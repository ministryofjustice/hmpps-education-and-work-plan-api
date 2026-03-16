package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InterestType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import java.time.LocalDate
import java.time.Period

private val log = KotlinLogging.logger {}

@Service
class AiGoalGenerator(private val chatClient: ChatClient) {
  companion object {
    val PROMPT_TEMPLATE =
      PromptTemplate("""
        Write a SMART (Specific, Measurable, Achievable, Relevant, and Time-bound) goal for this prisoner whilst they
        serve their time in prison.
        The goal should focus on their interests, previous work experience, and future aspirations; and should be relevant 
        to their rehabilitation and resettlement.
        
        The prisoner's name is {firstName}, they are {age} years old, and are {gender}.

        They were admitted to prison on {admissionDate} and are expected to be released on {releaseDate}.

        They have the following interests: {interests}.
        They have the following previous work experience: {previousWorkExperience}.
        They have the following future work aspirations: {futureAspirations}.        
      """.trimIndent())
  }

  fun generateGoal(prisoner: Prisoner, induction: Induction): GeneratedGoal? = chatClient
    .prompt(
      PROMPT_TEMPLATE
        .create(mapOf(
          "firstName" to prisoner.firstName,
          "age" to Period.between(prisoner.dateOfBirth, LocalDate.now()).years,
          "gender" to prisoner.gender,
          "admissionDate" to prisoner.receptionDate,
          "releaseDate" to prisoner.releaseDate,
          "interests" to prisonerInterests(induction),
          "previousWorkExperience" to previousWorkExperience(induction),
          "futureAspirations" to futureAspirations(induction),
        )
      ).also {
        log.debug { "Prompt: $it" }
      }
    )
    .call()
    .entity(GeneratedGoal::class.java)
}

data class GeneratedGoal(
  val title: String,
  val description: String,
  val completionDate: LocalDate,
  val steps: List<String>,
  val specific: String,
  val measurable: String,
  val achievable: String,
  val relevant: String,
  val timeBound: String,
)

private fun prisonerInterests(induction: Induction): String {
  val personalInterests = induction.personalSkillsAndInterests?.interests
    ?.mapNotNull { if (it.interestType !== InterestType.OTHER) it.interestType.toString() else it.interestTypeOther }
    ?: listOf("none")

  return personalInterests.joinToString(separator = ", ")
}

private fun previousWorkExperience(induction: Induction): String {
  val previousWorkExperiences = induction.previousWorkExperiences?.experiences
    ?.mapNotNull { if (!it.role.isNullOrBlank() && !it.details.isNullOrBlank()) "${it.role}: ${it.details}" else null }
    ?: listOf("none")

  return previousWorkExperiences.joinToString(separator = ", ")
}

private fun futureAspirations(induction: Induction): String {
  val futureWorkInterests = induction.futureWorkInterests?.interests
    ?.mapNotNull { if (it.workType !== WorkInterestType.OTHER) it.workType.toString() else it.workTypeOther }
    ?: listOf("none")

  return futureWorkInterests.joinToString(separator = ", ")
}
