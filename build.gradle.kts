import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "7.1.2"
  id("org.openapi.generator") version "7.10.0"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"

  id("jacoco")
  id("name.remal.integration-tests") version "5.0.0"

  `java-test-fixtures`
}

apply(plugin = "org.openapi.generator")

val postgresqlVersion = "42.7.4"
val kotlinLoggingVersion = "3.0.5"
val springdocOpenapiVersion = "2.7.0"
val hmppsSqsVersion = "5.2.0"
val awaitilityVersion = "4.2.2"
val wiremockVersion = "3.10.0"
val jsonWebTokenVersion = "0.12.6"
val nimbusJwtVersion = "10.0.1"
val testContainersVersion = "1.20.4"
val awsSdkVersion = "1.12.779"
val buildDirectory: Directory = layout.buildDirectory.get()

ext["jackson-bom.version"] = "2.18.1"

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable",
  )
}

jacoco {
  toolVersion = "0.8.12"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation(project("domain:learningandworkprogress"))
  implementation(project("domain:personallearningplan"))
  implementation(project("domain:timeline"))

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.1.0")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:$hmppsSqsVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenapiVersion")

  implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")

  // Test dependencies
  testImplementation(testFixtures(project("domain:learningandworkprogress")))
  testImplementation(testFixtures(project("domain:personallearningplan")))
  testImplementation(testFixtures(project("domain:timeline")))
  testImplementation("org.awaitility:awaitility-kotlin:$awaitilityVersion")

  // Integration test dependencies
  integrationTestImplementation("org.testcontainers:postgresql:$testContainersVersion")
  integrationTestImplementation("org.testcontainers:localstack:$testContainersVersion")
  integrationTestApi("com.amazonaws:aws-java-sdk-core:$awsSdkVersion") // Needed so Localstack has access to the AWS SDK V1 API
  integrationTestImplementation(testFixtures(project("domain:learningandworkprogress")))
  integrationTestImplementation(testFixtures(project("domain:personallearningplan")))
  integrationTestImplementation(testFixtures(project("domain:timeline")))

  // Test fixtures dependencies
  testFixturesImplementation("org.assertj:assertj-core:3.26.3")
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
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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
      fileTree(buildDirectory)
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
  executionData(fileTree(buildDirectory).include("jacoco/*.exec"))
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
  outputDir.set("$buildDirectory/generated")
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
  outputDir.set("$buildDirectory/generated")
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
      "models" to "PrisonerInPrisonSummary,PrisonPeriod,SignificantMovement,TransferDetail",
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
}

tasks.named("compileKotlin") {
  dependsOn("buildEducationAndWorkPlanModel")
  dependsOn("buildPrisonApiModel")
}

kotlin {
  kotlinDaemonJvmArgs = listOf("-Xmx1g")
  sourceSets["main"].apply {
    kotlin.srcDir("$buildDirectory/generated/src/main/kotlin")
  }
}

// Exclude generated code from linting
ktlint {
  filter {
    exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
  }
}
