# KStore
<img src="docs/images/kstore.svg" height="240" align="right"/> 

[![Build](https://github.com/xxfast/KStore/actions/workflows/build.yml/badge.svg)](https://github.com/xxfast/KStore/actions/workflows/build.yml)

[![Kotlin Beta](https://kotl.in/badges/beta.svg)](https://kotlinlang.org/docs/components-stability.html)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.xxfast/kstore?color=blue)](https://search.maven.org/search?q=g:io.github.xxfast)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-mac](http://img.shields.io/badge/platform-macos-111111.svg?style=flat)
![badge-watchos](http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat)
![badge-tvos](http://img.shields.io/badge/platform-tvos-808080.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![badge-linux](http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat)
![badge-nodejs](https://img.shields.io/badge/platform-jsNode-F8DB5D.svg?style=flat)
![badge-jsBrowser](https://img.shields.io/badge/platform-jsBrowser-F8DB5D.svg?style=flat)
![badge-wasmJsBrowser](https://img.shields.io/badge/platform-wasmJsBrowser-F8DB5D.svg?style=flat)

A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialization and kotlinx.io.
Inspired by [RxStore](https://github.com/Gridstone/RxStore)

## Features
  - 🔒 Read-write locks; with a mutex FIFO lock
  - 💾 In-memory caching; read once from disk and reuse
  - 📬 Default values; no file? no problem!
  - 🚚 Migration support; moving shop? take your data with you
  - 🚉 Multiplatform!

## Installation

KStore is published on Maven Central. Latest version [![Maven Central](https://img.shields.io/maven-central/v/io.github.xxfast/kstore?color=blue)](https://search.maven.org/search?q=g:io.github.xxfast)
```kotlin
repositories { 
  mavenCentral()
  // or for snapshot builds
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}
```

```toml
# for common
kstore = { module = "io.github.xxfast:kstore", version.ref = "kstore" }

# for android, iOS, desktop and nodeJs
kstore-file = { module = "io.github.xxfast:kstore-file", version.ref = "kstore" }

# for jsBrowser and wasmJsBrowser
kstore-storage = { module = "io.github.xxfast:kstore-storage", version.ref = "kstore" }
```

Depending on your target platforms, you will need to add platform configurations [here](https://xxfast.github.io/KStore/installation.html)

## At a glance

```kotlin
import kotlin.io.path.Path
import kotlinx.serialization.Serializable

// Take any serializable model 
@Serializable data class Pet(val name: String, val age: Int) 

// Create a store
val store: KStore<Pet> = storeOf(file = Path("path/to/my_cats.json"))

// Get, set, update or delete values 
val mylo: Pet? = store.get()
store.set(mylo)
store.update { pet: Pet? ->
  pet?.copy(age = pet.age + 1)
}
store.delete()

// Observe for updates
val pets: Flow<Pet?> = store.updates
```

### Installation and Usage

Documentation [here](https://xxfast.github.io/KStore)

API Reference [here](https://xxfast.github.io/KStore/docs/)

## Licence

    Copyright 2023 Isuru Rajapakse

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
