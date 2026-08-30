package io.github.xxfast.kstore.storage.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storage.delete
import io.github.xxfast.kstore.storage.get
import io.github.xxfast.kstore.storage.localStorage
import io.github.xxfast.kstore.storage.set
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable data class CatV1(val name: String, val cuteness: Int)
@Serializable data class CatV2(val name: String, val kawaiiness: Long)

private val MYLO_V1 = CatV1(name = "mylo", cuteness = 12)
private val MYLO_V2 = CatV2(name = "mylo", kawaiiness = 12L)

private const val KEY: String = "versioned_cat"

class KVersionedStoreTests {
  private val storeV1: KStore<CatV1> = storeOf(key = KEY, version = 1)

  private val storeV2: KStore<CatV2> = storeOf(
    key = KEY,
    version = 2
  ) { version, jsonElement ->
    when (version) {
      1 -> jsonElement?.jsonObject?.let {
        val name = it["name"]!!.jsonPrimitive.content
        val kawaiiness = it["cuteness"]!!.jsonPrimitive.int.toLong()
        CatV2(name, kawaiiness)
      }

      else -> null
    }
  }

  @AfterTest
  fun cleanup() {
    localStorage.delete()
  }

  @Test
  fun testStoreDelete() = runTest {
    storeV1.set(MYLO_V1)
    storeV1.delete()
    val expect: CatV2? = null
    val actual: CatV2? = storeV2.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testVersionIsStoredAlongsideData() = runTest {
    storeV2.set(MYLO_V2)
    assertEquals("2", localStorage["$KEY@version"])
  }

  @Test
  fun testMigrationV1ToV2() = runTest {
    storeV1.set(MYLO_V1)
    val expect: CatV2 = MYLO_V2
    val actual: CatV2? = storeV2.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testMigrationV2ToV1() = runTest {
    storeV2.set(MYLO_V2)
    val expect: CatV1? = null
    val actual: CatV1? = storeV1.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testDecodeMalformedRecord() = runTest {
    localStorage[KEY] = "💩"
    assertEquals(null, storeV2.get())
  }

  @Test
  fun testCorruptStoreRepairsOnNextWrite() = runTest {
    localStorage[KEY] = "💩"
    localStorage["$KEY@version"] = "💩"
    assertEquals(null, storeV2.get())

    storeV2.set(MYLO_V2)
    assertEquals(MYLO_V2, storeV2.get())
  }
}
