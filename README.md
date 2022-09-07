# <img src="https://kotlinlang.org/assets/images/favicon.svg" height="23"/> Store

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
