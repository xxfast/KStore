# <img src="https://kotlinlang.org/assets/images/favicon.svg" height="23"/> Store
[![Build](https://github.com/xxfast/KStore/actions/workflows/build.yml/badge.svg)](https://github.com/xxfast/KStore/actions/workflows/build.yml)

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.10-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.xxfast/kstore?color=blue)](https://search.maven.org/search?q=g:io.github.xxfast)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-mac](http://img.shields.io/badge/platform-macos-111111.svg?style=flat)
![badge-watchos](http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat)
![badge-tvos](http://img.shields.io/badge/platform-tvos-808080.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![badge-linux](http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat)
![badge-nodejs](https://img.shields.io/badge/platform-nodejs-F8DB5D.svg?style=flat)

A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialisation and okio.
Inspired by [RxStore](https://github.com/Gridstone/RxStore)

## Features
  - 🔒 Read-write locks; with a mutex FIFO lock
  - 💾 In-memory caching; read once from disk and reuse
  - 📬 Default values; no file? no problem!
  - 🚚 Migration support; moving shop? take your data with you
  - 🚉 Multiplatform!

## Adding to your project

KStore is published on Maven Central
```kotlin
repositories { 
  mavenCentral()
}
```

Include the dependency in `commonMain`. Latest version [![Maven Central](https://img.shields.io/maven-central/v/io.github.xxfast/kstore?color=blue)](https://search.maven.org/search?q=g:io.github.xxfast)
```kotlin
sourceSets {
  val commonMain by getting {
    implementation("io.github.xxfast:kstore:<version>")
  }
}
```

## Usage
Given that you have a `@Serializable` model
```kotlin
@Serializable data class Pet(val name: String, val age: Int) // Any serializable
val mylo = Pet(name = "Mylo", age = 1)
```

### Create a store
```kotlin
val store: KStore<Pet> = storeOf("path/to/file")
```
For full configuration and platform instructions, see [here](#configurations)

#### Get value

Get a value once

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
val mylo: Pet? = store.get()
```

Or observe for changes
```kotlin
val pets: Flow<Pet?> = store.updates
```

#### Set value  

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.set(mylo)
```

#### Update a value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.update { pet: Pet? ->
  pet?.copy(age = pet.age + 1)
}
```

Note: this maintains a single mutex lock transaction, unlike `get()` and a subsequent `set()`

#### Delete/Reset value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.delete()
```

You can also reset a value back to its default (if set, see [here](#configurations))

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.reset()
```

### Create a list store

KStore provides you with some convenient extensions to manage stores that contain lists. 
`listStoreOf` is the same as `storeOf`, but defaults to empty list instead of `null`
```kotlin
val listStore: KStore<List<Pet>> = listStoreOf("path/to/file") 
```

#### Get values

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
val pets: List<Cat> = listStore.getOrEmpty()
val pet: Cat = store.get(0)
```

or observe values

```kotlin
val pets: Flow<List<Cat>> = listStore.updatesOrEmpty
```

#### Add or remove elements

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
listStore.plus(cat)
listStore.minus(cat)
```

#### Map elements
<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
listStore.map { cat -> cat.copy(cat.age = cat.age + 1) }
listStore.mapIndexed { index, cat -> cat.copy(cat.age = index) }
```

## Configurations
Everything you want is in the factory method

```kotlin
private val store: KStore<Pet> = storeOf(
  // Required, see 🚉 Platform configurations 
  path = filePathTo("file.json"),

  // Returns this value if the file is not found. Defaults to null
  default = null,

  // Maintain a cache. If set to false, it always reads from disk
  enableCache = true,
  
  // Optional, see 🚚 Migrating stores
  version = 0, 
  migration = { version, jsonElement -> default },
  
  // Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
  serializer = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true 
  }, // optional
)
```

### 🚉 Platform configurations

Getting a path to a file is different for each platform and you will need to define how this works for each platform 
```kotlin
expect fun filePathTo(fileName: String): String
```

#### On Android
```kotlin
actual fun filePathTo(fileName: String): String = "${context.filesDir.path}/$fileName"
```

#### On iOS & other Apple platforms
```kotlin
actual fun filePathTo(fileName: String): String = "${NSHomeDirectory()}/$fileName"
```

#### On Desktop
This depends on where you want to save your files, but generally you should save your files in a user data directory.
Recommending to use [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir
```kotlin
actual fun filePathTo(fileName: String): String {
  // implementation("net.harawata:appdirs:1.2.1")
  val appDir: String = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, ORGANISATION)
  return "$appDir/$fileName"
}
```

#### On JS Browser

TODO

#### On NodeJS

TODO

### 🚚 Migrating stores
You can use the existing fields to derive the new fields without needing to write your own migrations

```kotlin
@Serializable data class CatV1(val name: String, val lives: Int = 9)
@Serializable data class CatV2(val name: String, val lives: Int = 9, val age: Int = 9 - lives)
```

#### Binary incompatible changes
If the new models are [binary incompatible](https://github.com/Kotlin/binary-compatibility-validator#what-makes-an-incompatible-change-to-the-public-binary-api) you will need to specify how to migrate the models from version to version

```kotlin
@Serializable data class CatV1(val name: String, val lives: Int = 9, val cuteness: Int) 
@Serializable data class CatV2(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val kawaiiness: Long)
@Serializable data class CatV3(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val isCute: Boolean)

private val storeV3: KStore<CatV3> = storeOf(filePath = filePath, version = 3) { version, jsonElement ->
  when (version) {
    1 -> jsonElement?.jsonObject?.let {
      val name = it["name"]!!.jsonPrimitive.content
      val lives = it["lives"]!!.jsonPrimitive.int
      val age = it["age"]?.jsonPrimitive?.int ?: (9 - lives)
      val isCute = it["cuteness"]!!.jsonPrimitive.int.toLong() > 1
      CatV3(name, lives, age, isCute)
    }

    2 -> jsonElement?.jsonObject?.let {
      val name = it["name"]!!.jsonPrimitive.content
      val lives = it["lives"]!!.jsonPrimitive.int
      val age = it["age"]?.jsonPrimitive?.int ?: (9 - lives)
      val isCute = it["kawaiiness"]!!.jsonPrimitive.long > 1
      CatV3(name, lives, age, isCute)
    }

    else -> null
  }
}
```

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
