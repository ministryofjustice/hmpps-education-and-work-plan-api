import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-library`
  `java-test-fixtures`
}

apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "jacoco")
apply(plugin = "com.adarshr.test-logger")

val kotlinLoggingVersion = "3.0.5"
repositories {
  mavenCentral()
}

dependencies {
  api("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

  // Test dependencies
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
  testImplementation("org.assertj:assertj-core:3.26.3")
  testImplementation("org.mockito:mockito-junit-jupiter:5.14.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

extensions.getByType(JacocoPluginExtension::class).apply {
  toolVersion = "0.8.11"
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
  }
}

tasks.test {
  useJUnitPlatform()
}

// Jacoco code coverage
tasks.named("test") {
  finalizedBy("jacocoTestReport")
}
tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    html.required.set(true)
  }
}
