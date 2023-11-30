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

val storeV3: KStore<CatV3> = storeOf(filePath = filePath, version = 3) { version, jsonElement ->
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
