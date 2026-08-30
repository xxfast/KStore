package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable data class KittenV1(val name: String, val cuteness: Int)
@Serializable data class KittenV2(val name: String, val kawaiiness: Long)

private val MYLO_V1 = KittenV1(name = "mylo", cuteness = 12)
private val MYLO_V2 = KittenV2(name = "mylo", kawaiiness = 12L)

/**
 * Exercises [VersionedCodec] against an in-memory backend, proving the decorator is backend-agnostic.
 * Backend-specific behaviors (staging files, atomicity) are covered by each backend's own tests.
 */
class VersionedCodecTests {
  private val records: MutableMap<String, String> = mutableMapOf()

  private inner class MapCodec<T : Any>(
    private val key: String,
    private val serializer: KSerializer<T>,
  ) : Codec<T> {
    override suspend fun encode(value: T?) {
      if (value != null) records[key] = DefaultJson.encodeToString(serializer, value)
      else records.remove(key)
    }

    override suspend fun decode(): T? =
      records[key]?.let { DefaultJson.decodeFromString(serializer, it) }
  }

  private inline fun <reified T : @Serializable Any> versionedCodecOf(
    version: Int,
    noinline migration: Migration<T, JsonElement> = DefaultMigration(null),
  ): Codec<T> = MapCodec(key = "data", serializer = serializer<T>()).versioned(
    version = version,
    versionCodec = MapCodec(key = "version", serializer = Int.serializer()),
    staleCodec = MapCodec(key = "data", serializer = JsonElement.serializer()),
    migration = migration,
  )

  private val codecV1: Codec<KittenV1> = versionedCodecOf(version = 1)

  private val codecV2: Codec<KittenV2> = versionedCodecOf(version = 2) { version, previous ->
    when (version) {
      1 -> previous?.jsonObject?.let {
        val name = it["name"]!!.jsonPrimitive.content
        val kawaiiness = it["cuteness"]!!.jsonPrimitive.int.toLong()
        KittenV2(name, kawaiiness)
      }

      else -> null
    }
  }

  @BeforeTest
  fun setup() {
    records.clear()
  }

  @Test
  fun testEncodeWritesDataAndVersion() = runTest {
    codecV1.encode(MYLO_V1)
    assertEquals("""{"name":"mylo","cuteness":12}""", records["data"])
    assertEquals("1", records["version"])
  }

  @Test
  fun testEncodeNullRemovesDataAndVersion() = runTest {
    codecV1.encode(MYLO_V1)
    codecV1.encode(null)
    assertEquals(emptyMap(), records)
  }

  @Test
  fun testDecodeCurrentVersion() = runTest {
    codecV2.encode(MYLO_V2)
    assertEquals(MYLO_V2, codecV2.decode())
  }

  @Test
  fun testDecodeMissing() = runTest {
    assertEquals(null, codecV2.decode())
  }

  @Test
  fun testMigration() = runTest {
    codecV1.encode(MYLO_V1)
    assertEquals(MYLO_V2, codecV2.decode())
  }

  @Test
  fun testMigrationWithoutAMigrationPathDecodesNull() = runTest {
    codecV2.encode(MYLO_V2)
    assertEquals(null, codecV1.decode())
  }

  @Test
  fun testStoreWithoutVersionReadsAsVersionZero() = runTest {
    records["data"] = """{"nine":"lives"}"""
    var seen: Int? = null
    val codec: Codec<KittenV2> = versionedCodecOf(version = 2) { version, _ -> seen = version; null }
    assertEquals(null, codec.decode())
    assertEquals(0, seen)
  }

  @Test
  fun testUnreadableVersionReadsAsUnknown() = runTest {
    codecV1.encode(MYLO_V1)
    records["version"] = "💩"
    var seen: Int? = 42
    val codec: Codec<KittenV2> = versionedCodecOf(version = 2) { version, _ -> seen = version; null }
    assertEquals(null, codec.decode())
    assertEquals(null, seen)
  }

  @Test
  fun testUnreadableDataMigratesWithNullPrevious() = runTest {
    records["data"] = """{"name":"mylo","cute""" // truncated, unreadable even as a JsonElement
    records["version"] = "1"
    var seen: JsonElement? = null
    val codec: Codec<KittenV2> = versionedCodecOf(version = 2) { _, previous -> seen = previous; null }
    assertEquals(null, codec.decode())
    assertEquals(null, seen)
  }

  @Test
  fun testCorruptStoreRepairsOnNextWrite() = runTest {
    records["data"] = "💩"
    records["version"] = "💩"
    assertEquals(null, codecV2.decode())

    codecV2.encode(MYLO_V2)
    assertEquals(MYLO_V2, codecV2.decode())
  }
}
