package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.assertFailsWith

@Serializable data class CatV0(val name: String, val lives: Int = 9)
@Serializable data class CatV1(val name: String, val lives: Int = 9, val cuteness: Int = 12)
@Serializable data class CatV2(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val kawaiiness: Long)
@Serializable data class CatV3(val name: String, val lives: Int = 9, val age: Int = 9 - lives, val isCute: Boolean)

@Serializable
data class CatV41(val name: String, val friends: Map<String, @Serializable(with = TodoSerializer::class) Int>) {
  object TodoSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Int", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Int = TODO("Not yet implemented")
    override fun serialize(encoder: Encoder, value: Int): Unit = TODO("Not yet implemented")
  }
}

@Serializable
data class CatV42(val name: String, val friends: Map<String, Int>)


val MYLO_V0 = CatV0(name = "mylo", lives = 7)
val MYLO_V1 = CatV1(name = "mylo", lives = 7, cuteness = 12)
val MYLO_V2 = CatV2(name = "mylo", lives = 7, age = 2, kawaiiness = 12L)
val MYLO_V3 = CatV3(name = "mylo", lives = 7, age = 2, isCute = true)
val MYLO_V41 = CatV41(name = "mylo", friends = mapOf("oreo" to 5, "kat" to 10))
val MYLO_V42 = CatV42(name = "mylo", friends = mapOf("oreo" to 5, "kat" to 10))

class KVersionedStoreTests {
  private val file: Path = Path("test_migration.json")

  private val storeV0: KStore<CatV0> = storeOf(file = file)

  private val storeV1: KStore<CatV1> = storeOf(file = file, version = 1)

  private val storeV2: KStore<CatV2> = storeOf(
    file = file,
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
    file = file,
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

  private val storeV41: KStore<CatV41> = storeOf(file = file, version = 4)
  private val storeV42: KStore<CatV42> = storeOf(file = file, version = 4)

  @AfterTest
  fun cleanup() {
    SystemFileSystem.delete(file, mustExist = false)
    SystemFileSystem.delete(Path("${file.name}.version"), mustExist = false)
    SystemFileSystem.delete(Path("${file.name}.temp"), mustExist = false)
    SystemFileSystem.delete(Path("${file.name}.version.temp"), mustExist = false)
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

  @Test
  fun testTransactionalEncode() = runTest {
    assertFailsWith<NotImplementedError> { storeV41.set(MYLO_V41) }
    assertEquals(null, storeV41.get())

    storeV42.set(MYLO_V42)
    assertFailsWith<NotImplementedError> { storeV41.set(MYLO_V41) }

    assertEquals(MYLO_V42, storeV42.get())
    assertFailsWith<NotImplementedError> { storeV41.get() }
  }
}
