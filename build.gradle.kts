buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.4.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.10")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }

  group = "io.github.xxfast"
  version = "0.5.0-SNAPSHOT"

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
}
