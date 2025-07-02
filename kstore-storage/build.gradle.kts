import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
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
    commonMain {
      dependencies {
        implementation(project(":kstore"))
        implementation(libs.kotlinx.serialization.json)
      }
    }

    wasmJsMain {
      dependencies {
        implementation(libs.kotlinx.browser)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}

//
// TODO: https://youtrack.jetbrains.com/issue/KT-63014/Running-tests-with-wasmJs-in-1.9.20-requires-Chrome-Canary#focus=Comments-27-8321383.0-0
// The following is required to support the wasmJs target.
//
// Node.js Canary is set to 21.0.0-v8-canary20231019bd785be450
// as that is the last version to ship Windows binaries too.
//

rootProject.extensions.configure<NodeJsRootExtension> {
  nodeVersion = "21.0.0-v8-canary20231019bd785be450"
  nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
}

rootProject.tasks.withType<KotlinNpmInstallTask>().configureEach {
  val flag = "--ignore-engines"

  if (!args.contains(flag)) {
    args.add(flag)
  }
}
