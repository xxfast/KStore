import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
}


kotlin {
  explicitApi()

  jvm {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }


  js(IR) {
    browser()
    nodejs()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    nodejs()
  }

  val appleTargets = listOf(
    macosX64(), macosArm64(),
    iosArm64(), iosX64(), iosSimulatorArm64(),
    watchosArm32(), watchosArm64(), watchosX64(),
    watchosSimulatorArm64(),
    tvosArm64(), tvosX64(), tvosSimulatorArm64(),
  )

  appleTargets.forEach { target ->
    with(target) {
      binaries {
        framework {
          baseName = "KStore"
        }
      }
    }
  }

  linuxX64()
  linuxArm64()
  mingwX64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.json)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.turbine)
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.junit)
        implementation(libs.junit.jupiter.api)
        implementation(libs.junit.jupiter.engine)
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

dependencies {
  kover(project(":kstore-file"))
  kover(project(":kstore-storage"))
}