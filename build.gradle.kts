buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.4.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.10")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}
