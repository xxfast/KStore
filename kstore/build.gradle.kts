import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinToolingSetupTask

plugins {
  kotlin("multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

kotlin {
  explicitApi()
  applyDefaultHierarchyTemplate()

  android {
    namespace = "io.github.xxfast.kstore"
    compileSdk = 36
    minSdk = 21

    withHostTestBuilder {}

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }

  jvm("desktop") {
    compilations.all {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.JVM_1_8)
      }
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  js {
    browser()
    nodejs()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    nodejs()
  }

  macosArm64()
  iosArm64(); iosSimulatorArm64()
  linuxX64(); mingwX64("windows")

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlinx.serialization.json)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.turbine)
    }

    named("androidHostTest") {
      dependencies {
        implementation(libs.junit)
        implementation(libs.junit.jupiter.api)
        implementation(libs.junit.jupiter.engine)
        implementation(libs.androidx.test.junit)
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

dependencies {
  kover(project(":kstore-file"))
  kover(project(":kstore-storage"))
}
