package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.Mapping

@Mapping(target = "id", ignore = true)
annotation class ExcludeIdField

@Mapping(target = "parent", ignore = true)
annotation class ExcludeParentEntity

@Mapping(target = "reference", expression = "java(java.util.UUID.randomUUID())")
annotation class GenerateNewReference
