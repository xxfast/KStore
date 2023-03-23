plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.0"
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
      }
    }

    val jsTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
      }
    }
  }
}


