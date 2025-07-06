import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
  id("org.jetbrains.kotlinx.kover") version "0.8.2"
  id("org.jetbrains.dokka") version "1.9.20"
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
    classpath(libs.vanniktech.maven.publish)
  }
}

// TODO: Migrate away from allprojects
allprojects {
  repositories {
    google()
    mavenCentral()
  }

  group = "io.github.xxfast"
  version = "1.1.0-SNAPSHOT"

  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  apply(plugin = "org.jetbrains.kotlinx.kover")
  apply(plugin = "com.vanniktech.maven.publish")
  apply(plugin = "org.jetbrains.dokka")

  extensions.configure<MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), project.name, version.toString())

    pom {
      name = "Kstore"
      description = "A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialisation and okio"
      url = "https://xxfast.github.io/KStore/"
      licenses {
        license {
          name = "Apache-2.0"
          url = "https://opensource.org/licenses/Apache-2.0"
        }
      }
      issueManagement {
        system = "Github"
        url = "https://github.com/xxfast/KStore/issues"
      }
      scm {
        developerConnection = "scm:git:ssh://git@github.com/xxfast/KStore.git"
        connection = "https://github.com/xxfast/KStore.git"
        url = "https://github.com/xxfast/KStore"
      }
      developers {
        developer {
          name = "Isuru Rajapakse"
          email = "isurukusumal36@gmail.com"
        }
      }
    }
  }
}
