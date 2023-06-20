import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.2.0"
  id("org.openapi.generator") version "6.6.0"
  kotlin("plugin.spring") version "1.8.22"
  kotlin("plugin.jpa") version "1.8.22"

  id("jacoco")
  id("name.remal.integration-tests") version "4.0.0"

  `java-test-fixtures`
}

apply(plugin = "org.openapi.generator")

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

  implementation("io.github.microutils:kotlin-logging:3.0.5")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.6.0")

  // Integration test dependencies
  integrationTestImplementation("com.h2database:h2")

  // Test fixtures dependencies
  testFixturesImplementation("org.assertj:assertj-core")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
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
        .include("libs/*-test-fixtures.jar")
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
  generatorName.set("kotlin")
  inputSpec.set("$projectDir/src/main/resources/openapi/EducationAndWorkPlanAPI.yml")
  outputDir.set("$buildDir/generated")
  modelPackage.set("uk.gov.justice.digital.hmpps.educationandworkplanapi.model")
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "serializationLibrary" to "jackson"
    )
  )
  globalProperties.set(
    mapOf(
      "models" to ""
    )
  )
}

tasks {
  withType<KtLintCheckTask> {
    // Under gradle 8 we must declare the dependency here, even if we're not going to be linting the model
    mustRunAfter("buildEducationAndWorkPlanModel")
  }
  withType<KtLintFormatTask> {
    // Under gradle 8 we must declare the dependency here, even if we're not going to be linting the model
    mustRunAfter("buildEducationAndWorkPlanModel")
  }
}

tasks.named("compileKotlin") {
  dependsOn("buildEducationAndWorkPlanModel")
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
