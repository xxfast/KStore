import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.2"
}

kotlin {
  explicitApi()

  js(IR) {
    browser()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    binaries.executable()
    browser()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":kstore"))
        implementation(libs.kotlinx.serialization.json)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}


