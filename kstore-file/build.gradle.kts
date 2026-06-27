import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

kotlin {
  explicitApi()
  applyDefaultHierarchyTemplate()

  android {
    namespace = "io.github.xxfast.kstore.file"
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
    nodejs()
  }

  macosArm64()
  iosArm64(); iosSimulatorArm64()
  linuxX64(); mingwX64("windows")

  sourceSets {
    commonMain.dependencies {
      implementation(project(":kstore"))
      api(libs.kotlinx.io)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlinx.serialization.json.io)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.turbine)
    }
  }
}
