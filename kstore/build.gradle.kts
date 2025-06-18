import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
}

android {
  compileSdk = 36

  defaultConfig {
    minSdk = 21
  }

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  lint {
    // TODO: Figure out why the linter is failing on CI
    abortOnError = false
  }

  namespace = "io.github.xxfast.kstore"
}

kotlin {
  explicitApi()

  androidTarget {
    compilations.all {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }

  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
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

  val macosX64 = macosX64()
  val macosArm64 = macosArm64()
  val iosArm64 = iosArm64()
  val iosX64 = iosX64()
  val iosSimulatorArm64 = iosSimulatorArm64()
  val watchosArm32 = watchosArm32()
  val watchosArm64 = watchosArm64()
  val watchosX64 = watchosX64()
  val watchosSimulatorArm64 = watchosSimulatorArm64()
  val tvosArm64 = tvosArm64()
  val tvosX64 = tvosX64()
  val tvosSimulatorArm64 = tvosSimulatorArm64()
  val appleTargets = listOf(
    macosX64, macosArm64,
    iosArm64, iosX64, iosSimulatorArm64,
    watchosArm32, watchosArm64, watchosX64,
    watchosSimulatorArm64,
    tvosArm64, tvosX64, tvosSimulatorArm64,
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

  linuxX64("linux")
  mingwX64("windows")

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.json)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.turbine)
      }
    }

    val androidMain by getting
    val androidUnitTest by getting {
      dependencies {
        implementation(libs.junit)
        implementation(libs.junit.jupiter.api)
        implementation(libs.junit.jupiter.engine)
        implementation(libs.androidx.test.junit)
      }
    }

    val desktopMain by getting
    val desktopTest by getting

    val jsMain by getting
    val jsTest by getting

    val wasmJsMain by getting
    val wasmJsTest by getting

    val appleMain by creating {
      dependsOn(commonMain)
    }
    val appleTest by creating

    appleTargets.forEach { target ->
      getByName("${target.targetName}Main") { dependsOn(appleMain) }
      getByName("${target.targetName}Test") { dependsOn(appleTest) }
    }

    val linuxMain by getting
    val linuxTest by getting

    val windowsMain by getting
    val windowsTest by getting
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
  kover(project(":kstore-test"))
}