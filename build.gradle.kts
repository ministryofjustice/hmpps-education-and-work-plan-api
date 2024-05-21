import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.3"
  id("org.openapi.generator") version "7.3.0"
  kotlin("plugin.spring") version "1.9.21"
  kotlin("plugin.jpa") version "1.9.21"
  kotlin("kapt") version "1.9.21"

  id("jacoco")
  id("name.remal.integration-tests") version "4.0.2"

  `java-test-fixtures`
}

apply(plugin = "org.openapi.generator")

val mapstructVersion = "1.5.5.Final"
val postgresqlVersion = "42.7.2"
val kotlinLoggingVersion = "3.0.5"
val springdocOpenapiVersion = "2.3.0"
val awaitilityVersion = "4.2.0"
val wiremockVersion = "3.4.1"
val jsonWebTokenVersion = "0.12.5"
val nimbusJwtVersion = "9.37.3"
val postgressTestContainersVersion = "1.19.7"

ext["jackson-bom.version"] = "2.16.1"

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable",
  )
}

kapt {
  arguments {
    arg("mapstruct.defaultComponentModel", "spring")
  }
}

jacoco {
  toolVersion = "0.8.11"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation(project("domain:learningandworkprogress"))
  implementation(project("domain:personallearningplan"))
  implementation(project("domain:timeline"))

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:0.2.2")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenapiVersion")

  implementation("org.mapstruct:mapstruct:$mapstructVersion")
  kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")

  implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")

  // Test dependencies
  testImplementation(testFixtures(project("domain:learningandworkprogress")))
  testImplementation(testFixtures(project("domain:personallearningplan")))
  testImplementation(testFixtures(project("domain:timeline")))
  testImplementation("org.awaitility:awaitility-kotlin:$awaitilityVersion")

  // Integration test dependencies
  integrationTestImplementation("org.testcontainers:postgresql:$postgressTestContainersVersion")
  integrationTestImplementation(testFixtures(project("domain:learningandworkprogress")))
  integrationTestImplementation(testFixtures(project("domain:personallearningplan")))
  integrationTestImplementation(testFixtures(project("domain:timeline")))

  // Test fixtures dependencies
  testFixturesImplementation("org.assertj:assertj-core")
  testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
  testFixturesImplementation("io.projectreactor:reactor-core")
  testFixturesImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  // Libraries to support creating JWTs in test fixtures
  testFixturesImplementation("io.jsonwebtoken:jjwt-impl:$jsonWebTokenVersion")
  testFixturesImplementation("io.jsonwebtoken:jjwt-jackson:$jsonWebTokenVersion")
  testFixturesImplementation("com.nimbusds:nimbus-jose-jwt:$nimbusJwtVersion")

  integrationTestImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
  testFixturesImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}

/*
  `dps-gradle-spring-boot` disabled the jar task in jira DT-2070, specifically to prevent the generation of the
  Spring Boot plain jar. The reason was that the `Dockerfile` copies the Spring Boot fat jar with:
  `COPY --from=builder --chown=appuser:appgroup /app/build/libs/hmpps-education-and-work-plan-api*.jar /app/app.jar`
  The fat jar includes the date in the filename, hence needing to use a wildcard. Using the wildcard causes problems
  if there are multiple matching files (eg: the plain jar)

  The plugin `java-test-fixtures` requires the plain jar in order to access the compiled classes from the `main` source
  root. The `jar` task has been re-enabled here to allow `java-test-fixtures` to see the `main` classes, and a hook
  has been added to the `assemble` task to remove the plain jar and test-fixtures jar before assembling the Spring Boot
  fat jar.
 */
tasks.named("jar") {
  enabled = true
}

tasks.named("assemble") {
  // `assemble` task assembles the classes and dependencies into a fat jar
  // Beforehand we need to remove the plain jar and test-fixtures jars if they exist
  doFirst {
    delete(
      fileTree(project.buildDir.absolutePath)
        .include("libs/*-plain.jar")
        .include("libs/*-test-fixtures.jar"),
    )
  }
}

tasks.named("check") {
  dependsOn("integrationTest")
}

// Jacoco code coverage
tasks.named("test") {
  finalizedBy("jacocoTestReport")
}
tasks.named("integrationTest") {
  finalizedBy("jacocoIntegrationTestReport")
}

tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    html.required.set(true)
  }
}
tasks.named<JacocoReport>("jacocoIntegrationTestReport") {
  reports {
    html.required.set(true)
  }
}

tasks.register<JacocoReport>("combineJacocoReports") {
  executionData(fileTree(project.buildDir.absolutePath).include("jacoco/*.exec"))
  classDirectories.setFrom(files(project.sourceSets.main.get().output))
  sourceDirectories.setFrom(files(project.sourceSets.main.get().allSource))
  reports {
    html.required.set(true)
  }
}

tasks.register<GenerateTask>("buildEducationAndWorkPlanModel") {
  validateSpec.set(true)
  generatorName.set("kotlin-spring")
  templateDir.set("$projectDir/src/main/resources/static/openapi/templates")
  inputSpec.set("$projectDir/src/main/resources/static/openapi/EducationAndWorkPlanAPI.yml")
  outputDir.set("$buildDir/generated")
  modelPackage.set("uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model")
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "serializationLibrary" to "jackson",
      "useBeanValidation" to "true",
      "useSpringBoot3" to "true",
      "enumPropertyNaming" to "UPPERCASE",
    ),
  )
  globalProperties.set(
    mapOf(
      "models" to "",
    ),
  )
}

tasks.register<GenerateTask>("buildPrisonApiModel") {
  validateSpec.set(true)
  generatorName.set("kotlin-spring")
  templateDir.set("$projectDir/src/main/resources/static/openapi/templates")
  inputSpec.set("$projectDir/src/main/resources/static/openapi/PrisonApi.json")
  outputDir.set("$buildDir/generated")
  modelPackage.set("uk.gov.justice.digital.hmpps.prisonapi.resource.model")
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "serializationLibrary" to "jackson",
      "useBeanValidation" to "true",
      "useSpringBoot3" to "true",
      "enumPropertyNaming" to "UPPERCASE",
    ),
  )
  globalProperties.set(
    mapOf(
      "models" to "",
    ),
  )
}

tasks {
  withType<KtLintCheckTask> {
    // Under gradle 8 we must declare the dependency here, even if we're not going to be linting the model
    mustRunAfter("buildEducationAndWorkPlanModel")
    mustRunAfter("buildPrisonApiModel")
  }
  withType<KtLintFormatTask> {
    // Under gradle 8 we must declare the dependency here, even if we're not going to be linting the model
    mustRunAfter("buildEducationAndWorkPlanModel")
    mustRunAfter("buildPrisonApiModel")
  }
  withType<KaptGenerateStubsTask> {
    mustRunAfter("buildEducationAndWorkPlanModel")
    mustRunAfter("buildPrisonApiModel")
  }
}

tasks.named("compileKotlin") {
  dependsOn("buildEducationAndWorkPlanModel")
  dependsOn("buildPrisonApiModel")
}

kotlin {
  sourceSets["main"].apply {
    kotlin.srcDir("$buildDir/generated/src/main/kotlin")
  }
}

// Exclude generated code from linting
ktlint {
  filter {
    exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
  }
}
