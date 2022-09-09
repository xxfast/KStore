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

## Usage
Say you have a model
```kotlin
@Serializable data class Pet(val name: String, val age: Int) // Any serializable

val mylo = Pet(name = "Mylo", age = 1)
```

### Crate a store
```kotlin

val storeOf: KStore<Pet> = store("whatever.json".toPath())
```
full confuguration [here](#configurations)

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

### Clear value

<img src="https://user-images.githubusercontent.com/13775137/188902401-121fd1a2-c506-4982-82dd-c8c4404c81a0.png" align="right"/>

```kotlin
store.clear()
```

## Configurations

Everything you want in the factory me
```kotlin
private val store: KStore<Pet> = storeOf(
  path = "whatever.json".toPath(), // path to file, required
  default = null, // optional
  enableCache = true, , // optional
  serializer = Json, // optional
)
```
