plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.2"
}

kotlin {
  explicitApi()

  js(IR) {
    browser()
  }

  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(project(":kstore"))
        implementation(libs.kotlinx.serialization.json)
      }
    }

    val jsTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}


