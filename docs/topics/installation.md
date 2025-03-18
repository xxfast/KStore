# Installation

KStore is published on Maven Central
```kotlin
repositories { 
  mavenCentral()
  // or for snapshot builds
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}
```

Latest version [![Maven Central](https://img.shields.io/maven-central/v/io.github.xxfast/kstore?color=blue)](https://search.maven.org/search?q=g:io.github.xxfast)

```toml
[versions]
kstore = "x.x.x" 

[libraries]
kstore = { module = "io.github.xxfast:kstore", version.ref = "kstore" }
kstore-file = { module = "io.github.xxfast:kstore-file", version.ref = "kstore" }
kstore-storage = { module = "io.github.xxfast:kstore-storage", version.ref = "kstore" }
kstore-test = { module = "io.github.xxfast:kstore-test", version.ref = "kstore" }
```

### Targeting Android, iOS and/or Desktop 

You can include the kstore-file dependency in `commonMain`
```kotlin
sourceSets {
  val commonMain by getting {
    dependencies {
      implementation(libs.kstore.file)
    }
  }
}
```

> **Note** -  This will also work for non-browser js targets, like node

### Targeting Android, iOS, Desktop and/or Web

You will need to split up the dependencies as browsers don't support a file system
```kotlin
sourceSets {
  val commonMain by getting {
    dependencies {
      implementation(libs.kstore)
    }
  }
  
  val androidMain by getting { 
    dependencies { 
      implementation(libs.kstore.file) 
    } 
  }
  
  val iosMain by getting { 
    dependencies { 
      implementation(libs.kstore.file) 
    } 
  }
  
  val jvmMain by getting { 
    dependencies { 
      implementation(libs.kstore.file) 
    } 
  }

  val jsMain by getting {
    dependencies {
      implementation(libs.kstore.storage)
    }
  }
}
```
