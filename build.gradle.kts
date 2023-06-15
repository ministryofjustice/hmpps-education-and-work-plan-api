plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.2.0"
  kotlin("plugin.spring") version "1.8.21"

  id("jacoco")
  id("name.remal.integration-tests") version "4.0.0"

  `java-test-fixtures`
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
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
