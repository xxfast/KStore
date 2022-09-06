plugins {
  kotlin("multiplatform") version "1.7.10"
  kotlin("plugin.serialization") version "1.7.10"
}

group = "io.github.xxfast"
version = "0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
    }
    withJava()
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  js(BOTH) {
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
      }
    }
  }

  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }


  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-okio:1.4.0")

      }
    }

    val commonTest by getting {
      dependsOn(commonMain)
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }
    val jvmTest by getting {
      dependsOn(jvmMain)
    }

    val jsMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }
    val jsTest by getting

    val nativeMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }
    val nativeTest by getting
  }
}
