package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.ManageUsersApiClient
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Component
class BaseController(private val usersClient: ManageUsersApiClient) {
  fun <T : Any> populateDisplayName(response: T): T {
    val clazz = response::class

    // Get the primary constructor of the class
    val constructor = clazz.primaryConstructor?.apply { isAccessible = true }
      ?: return response // If no primary constructor, return the original response

    // Prepare arguments for the constructor
    val args = mutableMapOf<String, Any?>()

    // Populate the displayName fields based on createdBy and updatedBy values
    clazz.memberProperties.forEach { property ->
      when (property.name) {
        "createdBy" -> {
          val createdBy = property.getter.call(response)
          if (createdBy != null) {
            val createdByDisplayName = usersClient.getUserDetails(createdBy as String)?.name
            args["createdByDisplayName"] = createdByDisplayName
          }
        }

        "updatedBy" -> {
          val updatedBy = property.getter.call(response)
          if (updatedBy != null) {
            val updatedByDisplayName = usersClient.getUserDetails(updatedBy as String)?.name
            args["updatedByDisplayName"] = updatedByDisplayName
          }
        }
      }
    }

    // Call the constructor with either the updated or the original property values
    val params = constructor.parameters.associateWith { parameter ->
      args[parameter.name] ?: clazz.memberProperties.find { it.name == parameter.name }?.getter?.call(response)
    }

    @Suppress("UNCHECKED_CAST")
    return constructor.callBy(params) as? T ?: response
  }
}
