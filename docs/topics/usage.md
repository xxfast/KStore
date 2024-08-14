# How to Use KStore

## Create your store
Initialise your stores and keep a reference to it in your app (_preferably_ using DI so that they are reused)

### KStore File
```kotlin
val store: KStore<Pet> = storeOf(file = Path("$appDir/my_cats.json"))
```

> **NOTE** - Setting up the `appDir` is covered [here](using-platform-paths.md)

### KStore Storage
```kotlin
  val store: KStore<Pet> = storeOf(key = "my_cats")
```

### Other configuration options
Everything you want is in factory methods

```kotlin
val store: KStore<Pet> = storeOf(
  // For kstore-file with json
  file = Path("$appDir/$fileName.json"),

  // Or for kstore-storage
  key = "$keyName",
  storage = localStorage, // optional
  
  // Or your own custom codec 
  codec = YourCustomCodec<Pet>(), // optional

  // Returns this value if the file is not found. Defaults to null
  default = null, // optional

  // Maintain a cache. If set to false, it always reads from disk
  enableCache = true, // optional

  // For versioning
  version = 0, // optional
  migration = { version, jsonElement -> default }, // optional

  // For kstore-file, the serializer to use. 
  serializer = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }, // optional
)
```
{ collapsible="true" collapsed-title="val store: KStore<Pet> = storeOf"  }

## Use your store

Given that you have a `@Serializable` model and a value
```kotlin
@Serializable data class Pet(val name: String, val age: Int) // Any serializable
val mylo = Pet(name = "Mylo", age = 1)
```

#### Get value
Get a value once 

```kotlin
val mylo: Pet? = store.get()
```

Or observe for changes
```kotlin
val pets: Flow<Pet?> = store.updates
```

#### Set value
```kotlin
store.set(mylo)
```

#### Update a value
```kotlin
store.update { pet: Pet? ->
  pet?.copy(age = pet.age + 1)
}
```

> **Note!** - this maintains a single mutex lock transaction, unlike `get()` and a subsequent `set()`

#### Delete/Reset value
```kotlin
store.delete()
```

You can also reset a value back to its default (if set, see [here](#other-configuration-options))

```kotlin
store.reset()
```