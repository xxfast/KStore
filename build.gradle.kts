buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.4.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.0")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}
