import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinToolingSetupTask

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

kotlin {
  explicitApi()

  js {
    browser()
  }

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

    val wasmJsMain by getting {
      dependencies {
        implementation(libs.kotlinx.browser)
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

//
// TODO: https://youtrack.jetbrains.com/issue/KT-63014/Running-tests-with-wasmJs-in-1.9.20-requires-Chrome-Canary#focus=Comments-27-8321383.0-0
// The following is required to support the wasmJs target.
//
// Node.js Canary is set to 21.0.0-v8-canary20231019bd785be450
// as that is the last version to ship Windows binaries too.
//

rootProject.extensions.configure<WasmNodeJsEnvSpec> {
  version.set("21.0.0-v8-canary20231019bd785be450")
  downloadBaseUrl.set("https://nodejs.org/download/v8-canary")
}

rootProject.tasks.withType<KotlinNpmInstallTask>().configureEach {
  val flag = "--ignore-engines"

  if (!args.contains(flag)) {
    args.add(flag)
  }
}

rootProject.tasks.withType<KotlinToolingSetupTask>().configureEach {
  args.add("--ignore-engines")
}
