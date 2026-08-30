# Versioning Stores

You can use the existing fields to derive the new fields without needing to write your own migrations

```kotlin
@Serializable data class CatV1(val name: String, val lives: Int = 9)
@Serializable data class CatV2(val name: String, val lives: Int = 9, val age: Int = 9 - lives)
```

## Binary incompatible changes
If the new models are [binary incompatible](https://github.com/Kotlin/binary-compatibility-validator#what-makes-an-incompatible-change-to-the-public-binary-api) you will need to specify how to migrate the data from version to version

```kotlin
@Serializable
data class CatV1(
  val name: String,
  val lives: Int = 9,
  val cuteness: Int
)

@Serializable
data class CatV2(
  val name: String,
  val lives: Int = 9,
  val age: Int = 9 - lives, // derived field
  val kawaiiness: Long // new field
)

@Serializable
data class CatV3(
  val name: String,
  val lives: Int = 9, 
  val age: Int = 9 - lives, 
  val isCute: Boolean // renamed field 
)
```

### KStore File

```kotlin
val storeV3: KStore<CatV3> = storeOf(file = file, version = 3) { version, jsonElement ->
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

The version is kept in a sibling file on the same path with a `.version` suffix. Override it with `versionPath`.

### KStore Storage

Same signature, same migration, keyed instead of filed

```kotlin
val storeV3: KStore<CatV3> = storeOf(key = "my_cats", version = 3) { version, jsonElement ->
  // identical to above
}
```

The version is kept in a sibling record under `my_cats@version`. Override it with `versionKey`.

## Versioning your own codec

Both factories above are just a `Codec` decorated with `VersionedCodec`. Decorate your own codec with `versioned` to get the same behaviour

```kotlin
val codec: Codec<CatV3> = YourCodec<CatV3>(...).versioned(
  version = 3,

  // persists the version alongside the data
  versionCodec = YourCodec<Int>(...),

  // re-reads the old payload in a raw shape that survives schema changes
  staleCodec = YourCodec<JsonElement>(...),

  migration = { version, previous -> /* recover a CatV3, or null */ },
)

val store: KStore<CatV3> = storeOf(codec = codec)
```

Nothing in `VersionedCodec` knows about files or storage. Versioning is itself just persistence, so it is delegated to codecs, which is why the file and storage backends share it.

Three behaviours worth knowing:

- Data is written first, version second. A crash in between leaves the new data with a stale version, which still decodes directly. The other order would claim a version the data was never written to.
- No version record means the store predates versioning, and reads as version `0`.
- A version record that exists but cannot be read leaves `version` as `null`, the same way an unreadable payload leaves `previous` as `null`. Migrate what you can, return `null` when you cannot.
