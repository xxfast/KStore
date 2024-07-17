# KStore
<img src="kstore.svg" width="100" alt="logo"/>

[![Build](https://github.com/xxfast/KStore/actions/workflows/build.yml/badge.svg)](https://github.com/xxfast/KStore/actions/workflows/build.yml)

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
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
![badge-browser](https://img.shields.io/badge/platform-jsBrowser-F8DB5D.svg?style=flat)

A tiny Kotlin multiplatform library that assists in saving and restoring objects to and from disk using kotlinx.coroutines, kotlinx.serialization and okio.
Inspired by [RxStore](https://github.com/Gridstone/RxStore)

## Features
- 🔒 Read-write locks; with a mutex FIFO lock
- 💾 In-memory caching; read once from disk and reuse
- 📬 Default values; no file? no problem!
- 🚚 Migration support; moving shop? take your data with you
- 🚉 Multiplatform!

## At a glance

```kotlin
// Take any serializable model 
@Serializable data class Pet(val name: String, val age: Int) 

// Create a store
val store: KStore<Pet> = storeOf(filePath = "path/to/my_cats.json")

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


<seealso style="cards">
  <category ref="external">
    <a href="https://github.com/xxfast/KStore">GitHub</a>
    <a href="https://xxfast.github.io/KStore/api/">API Doc</a>
  </category>
</seealso>
