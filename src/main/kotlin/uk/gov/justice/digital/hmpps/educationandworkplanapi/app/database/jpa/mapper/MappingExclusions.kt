package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapping

@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
annotation class ExcludeJpaManagedFields

@ExcludeJpaManagedFields
@Mapping(target = "createdByDisplayName", ignore = true)
@Mapping(target = "updatedByDisplayName", ignore = true)
annotation class ExcludeJpaManagedFieldsIncludingDisplayNameFields

@Mapping(target = "reference", ignore = true)
annotation class ExcludeReferenceField

@Mapping(target = "reference", expression = "java(UUID.randomUUID())")
annotation class GenerateNewReference
