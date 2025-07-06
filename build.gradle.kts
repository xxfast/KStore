import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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
  apply(plugin = "com.vanniktech.maven.publish")
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "maven-publish")

  extensions.configure<MavenPublishBaseExtension> {
    val javadocJar = tasks.register<Jar>("javadocJar") {
      dependsOn(tasks.dokkaHtml)
      archiveClassifier.set("javadoc")
      from("$buildDir/dokka")
    }

    mavenPublishing {
      artifacts.dokka(javadocJar)
      val isSnapshot = version.toString().endsWith("SNAPSHOT")
      publishToMavenCentral(
        if (isSnapshot) {
          SonatypeHost("https://central.sonatype.com/repository/maven-snapshots/")
        } else {
          SonatypeHost.CENTRAL_PORTAL
        }
      )
      /**
       *  IMPORTANT!!!
       *  Review documentation Vanniktech documentation to properly pass credentials and sign for Central Sonatype
       *  https://vanniktech.github.io/gradle-maven-publish-plugin/central/#secrets
       *  If you need an example of implementation, try this:
       *  ```
       *       - name: Publish to MavenCentral
       *         if: ${{ github.event_name != 'pull_request' }}
       *         run: ./gradlew publishToMavenCentral --no-configuration-cache
       *         env:
       *           ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
       *           ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
       *           ORG_GRADLE_PROJECT_signingInMemoryKeyId: ${{ secrets.SIGNING_KEY_ID }}
       *           ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.SIGNING_PASSWORD }}
       *           ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.GPG_KEY_CONTENTS }}
       *  ```
       */
      signAllPublications()
      coordinates(group.toString(), project.name, version.toString())
    }

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
