buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.2.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.7.10")
  }
}

allprojects {
  repositories {
    mavenCentral()
  }
}
