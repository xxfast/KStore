package io.github.xxfast.kstore

import io.github.xxfast.kstore.extensions.storeOf
import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable data class CatV0(val name: String, val lives: Int = 9)
@Serializable data class CatV1(val name: String, val lives: Int = 9, val cuteness: Int = 12)
@Serializable data class CatV2(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val kawaiiness: Long)
@Serializable data class CatV3(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val isCute: Boolean)

val MYLO_V0 = CatV0(name = "mylo", lives = 7)
val MYLO_V1 = CatV1(name = "mylo", lives = 7, cuteness = 12)
val MYLO_V2 = CatV2(name = "mylo", lives = 7, age = 2, kawaiiness = 12L)
val MYLO_V3 = CatV3(name = "mylo", lives = 7, age = 2, isCute = true)

class KVersionedStoreTests {
  private val filePath: String = "test_migration.json"

  private val storeV0: KStore<CatV0> = storeOf(filePath = filePath)

  private val storeV1: KStore<CatV1> = storeOf(filePath = filePath, version = 1)

  private val storeV2: KStore<CatV2> = storeOf(
    filePath = filePath,
    version = 2
  ) { version, jsonElement ->
    when (version) {
      1 -> jsonElement?.jsonObject?.let {
        val name = it["name"]!!.jsonPrimitive.content
        val lives = it["lives"]!!.jsonPrimitive.int
        val age = it["age"]?.jsonPrimitive?.int ?: (9 - lives)
        val kawaiiness = it["cuteness"]!!.jsonPrimitive.int.toLong()
        CatV2(name, lives, age, kawaiiness)
      }

      else -> null
    }
  }

  private val storeV3: KStore<CatV3> = storeOf(
    filePath = filePath,
    version = 3
  ) { version, jsonElement ->
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

  @AfterTest
  fun cleanup() {
    FILE_SYSTEM.delete(filePath.toPath())
    FILE_SYSTEM.delete("$filePath.version".toPath())
  }

  @Test
  fun testStoreDelete()  = runTest {
    storeV1.delete()
    val expect: CatV2? = null
    val actual: CatV2? = storeV2.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testMigrationV0ToV1BinaryCompatible() = runTest {
    storeV0.set(MYLO_V0)
    val expect: CatV1 = MYLO_V1
    val actual: CatV1? = storeV1.get()
    assertEquals(expect, actual)
  }


  @Test
  fun testMigrationV1ToV2() = runTest {
    storeV1.set(MYLO_V1)
    val expect: CatV2 = MYLO_V2
    val actual: CatV2? = storeV2.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testMigrationV2ToV3() = runTest {
    storeV2.set(MYLO_V2)
    val expect: CatV3 = MYLO_V3
    val actual: CatV3? = storeV3.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testMigrationV1ToV3() = runTest {
    storeV1.set(MYLO_V1)
    val expect: CatV3 = MYLO_V3
    val actual: CatV3? = storeV3.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testMigrationV3ToV2() = runTest {
    storeV3.set(MYLO_V3)
    val expect: CatV2? = null
    val actual: CatV2? = storeV2.get()
    assertEquals(expect, actual)
  }
}
