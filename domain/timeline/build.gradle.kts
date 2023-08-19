plugins {
  `java-library`
  `java-test-fixtures`
}

apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "jacoco")
apply(plugin = "com.adarshr.test-logger")

repositories {
  mavenCentral()
}

dependencies {
  // Test dependencies
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
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
