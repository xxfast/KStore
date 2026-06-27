import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
  id("org.jetbrains.kotlinx.kover") version "0.9.8"
  id("org.jetbrains.dokka") version "2.2.0"
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
  version = "1.0.0"

  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  apply(plugin = "org.jetbrains.kotlinx.kover")
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "maven-publish")
  apply(plugin = "signing")

  extensions.configure<PublishingExtension> {
    repositories {
      maven {
        val isSnapshot = version.toString().endsWith("SNAPSHOT")
        url = uri(
          if (!isSnapshot) "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
          else "https://s01.oss.sonatype.org/content/repositories/snapshots"
        )

        credentials {
          username = gradleLocalProperties(rootDir, providers).getProperty("sonatypeUsername")
          password = gradleLocalProperties(rootDir, providers).getProperty("sonatypePassword")
        }
      }
    }

    val javadocJar = tasks.register<Jar>("javadocJar") {
      dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
      archiveClassifier.set("javadoc")
      from(layout.buildDirectory.dir("dokka/html"))
    }

    publications {
      withType<MavenPublication> {
        artifact(javadocJar)

        pom {
          name.set("KStore")
          description.set("A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialisation and okio")
          licenses {
            license {
              name.set("Apache-2.0")
              url.set("https://opensource.org/licenses/Apache-2.0")
            }
          }
          url.set("https://xxfast.github.io/KStore/")
          issueManagement {
            system.set("Github")
            url.set("https://github.com/xxfast/KStore/issues")
          }
          scm {
            connection.set("https://github.com/xxfast/KStore.git")
            url.set("https://github.com/xxfast/KStore")
          }
          developers {
            developer {
              name.set("Isuru Rajapakse")
              email.set("isurukusumal36@gmail.com")
            }
          }
        }
      }
    }
  }

  val publishing = extensions.getByType<PublishingExtension>()
  extensions.configure<SigningExtension> {
    useInMemoryPgpKeys(
      gradleLocalProperties(rootDir, providers).getProperty("gpgKeySecret"),
      gradleLocalProperties(rootDir, providers).getProperty("gpgKeyPassword"),
    )

    sign(publishing.publications)
  }

  // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
  project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
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
