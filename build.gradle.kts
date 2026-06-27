plugins {
  id("org.jetbrains.kotlinx.kover") version "0.9.8"
  id("org.jetbrains.dokka") version "2.2.0"
  id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath(libs.agp)
    classpath(libs.kotlin)
    classpath(libs.kotlin.serialization)
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }

  group = "io.github.xxfast"
  version = "1.1.0-SNAPSHOT"

  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  apply(plugin = "org.jetbrains.kotlinx.kover")
  apply(plugin = "org.jetbrains.dokka")
}

subprojects {
  apply(plugin = "com.vanniktech.maven.publish")

  // Publishes to the Sonatype Central Portal (central.sonatype.com). Credentials/signing
  // come from Gradle properties: mavenCentralUsername/mavenCentralPassword and
  // signingInMemoryKey/signingInMemoryKeyPassword (see CI env / ~/.gradle/gradle.properties).
  configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral() // no automaticRelease: deployment waits for manual "Publish" on the portal
    signAllPublications()

    pom {
      name.set("KStore")
      description.set("A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines and kotlinx.serialization")
      url.set("https://xxfast.github.io/KStore/")
      licenses {
        license {
          name.set("Apache-2.0")
          url.set("https://opensource.org/licenses/Apache-2.0")
        }
      }
      issueManagement {
        system.set("Github")
        url.set("https://github.com/xxfast/KStore/issues")
      }
      scm {
        url.set("https://github.com/xxfast/KStore")
        connection.set("scm:git:git://github.com/xxfast/KStore.git")
        developerConnection.set("scm:git:ssh://git@github.com/xxfast/KStore.git")
      }
      developers {
        developer {
          id.set("xxfast")
          name.set("Isuru Rajapakse")
          email.set("isurukusumal36@gmail.com")
        }
      }
    }
  }
}

// Dokka v2 multi-module aggregation: root project aggregates all submodule docs.
// Running `./gradlew dokkaGenerate` (or `dokkaGeneratePublicationHtml`) at root level
// produces combined HTML documentation in build/dokka/html/.
dependencies {
  dokka(project(":kstore"))
  dokka(project(":kstore-file"))
  dokka(project(":kstore-storage"))
}
