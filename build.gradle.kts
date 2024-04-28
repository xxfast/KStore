import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
  id("org.jetbrains.kotlinx.kover") version "0.6.1"
  id("org.jetbrains.dokka") version "1.9.10"
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
  version = "0.8.0-SNAPSHOT"

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
          username = gradleLocalProperties(rootDir).getProperty("sonatypeUsername")
          password = gradleLocalProperties(rootDir).getProperty("sonatypePassword")
        }
      }
    }

    val javadocJar = tasks.register<Jar>("javadocJar") {
      dependsOn(tasks.dokkaHtml)
      archiveClassifier.set("javadoc")
      from("$buildDir/dokka")
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
      gradleLocalProperties(rootDir).getProperty("gpgKeySecret"),
      gradleLocalProperties(rootDir).getProperty("gpgKeyPassword"),
    )

    sign(publishing.publications)
  }

  // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
  project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
  }
}

koverMerged {
  enable()
}
