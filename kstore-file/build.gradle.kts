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
    sourceSets {
      commonMain {
        dependencies {
          implementation(project(":kstore"))
          api(libs.kotlinx.io)
          implementation(libs.kotlinx.coroutines)
          implementation(libs.kotlinx.serialization.json.io)
        }
      }

      commonTest {
        dependencies {
          implementation(kotlin("test"))
          implementation(libs.kotlinx.coroutines.test)
          implementation(libs.turbine)
        }
      }
    }
  }
}
