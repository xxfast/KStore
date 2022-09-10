# <img src="https://kotlinlang.org/assets/images/favicon.svg" height="23"/> Store
[![Build](https://github.com/xxfast/KStore/actions/workflows/build.yml/badge.svg)](https://github.com/xxfast/KStore/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.10-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)

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

A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialisation and okio

## Features
  - 🔒 Read-write locks; with a mutex FIFO lock
  - 💾 In-memory caching; read once from disk and reuse
  - 🕺 Multiplatform!

## Adding to your project

KStore is not _yet_ published to Maven Central, but is available on sonatype snapshot repositories.
```kotlin
repositories {
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}
```

Include the dependency in `commonMain`
```kotlin
sourceSets {
  val commonMain by getting {
    implementation("io.github.xxfast:kstore:0.1-SNAPSHOT")
  }
}
```

## Usage
Given that you have a `@Serializable` model
```kotlin
@Serializable data class Pet(val name: String, val age: Int) // Any serializable
val mylo = Pet(name = "Mylo", age = 1)
```

### Crate a store
```kotlin
val storeOf: KStore<Pet> = store("path/to/file")
```
For full configuration and platform instructions, see [here](#configurations)

### Set value  

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.set(mylo)
```

### Get value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
val mylo: Pet? = store.get()
```

### Update a value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.update { pet: Pet? ->
  pet?.copy(age = pet.age + 1)
}
```

### Delete/Reset value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.delete()
```

You can also reset a value back to its default (if set, see [here](#configurations))

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.reset()
```

## Configurations

Everything you want is in the factory method
```kotlin
private val store: KStore<Pet> = storeOf(
  path = filePathTo("file.json"), // required
  default = null, // optional
  enableCache = true, // optional
  serializer = Json, // optional
)
```

### Platform configurations

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
Here i'm using [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir
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
