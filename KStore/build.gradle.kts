plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("com.android.library")
}

group = "io.github.xxfast"
version = "0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

android {
  compileSdk = 31
  defaultConfig {
    minSdk = 21
    targetSdk = 31
  }

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

kotlin {
  android {
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
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
      }
    }
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
        implementation("app.cash.turbine:turbine:0.9.0")
      }
    }

    val androidMain by getting
    val androidTest by getting {
      dependsOn(androidMain)
    }

    val desktopMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }
    val desktopTest by getting {
      dependsOn(desktopMain)
    }

    val jsMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }

    val jsTest by getting {
      dependsOn(jsMain)
    }

    val appleMain by creating {
      dependsOn(commonMain)
    }

    val appleTest by creating {
      dependsOn(appleMain)
    }

    appleTargets.forEach{ target ->
      getByName("${target.targetName}Main") { dependsOn(appleMain) }
      getByName("${target.targetName}Test") { dependsOn(appleTest) }
    }

    val nativeMain by getting {
      dependencies {
        implementation("com.squareup.okio:okio:3.2.0")
      }
    }

    val nativeTest by getting {
      dependsOn(nativeMain)
    }
  }
}
