package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.file.utils.FILE_SYSTEM
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as decode

class FileCodecTests {
  private val codec: FileCodec<List<Pet>> = FileCodec(file = FILE_PATH.toPath())

  @OptIn(ExperimentalSerializationApi::class)
  private var stored: List<Pet>?
    get() = FILE_SYSTEM.source(FILE_PATH.toPath()).buffer().use { DefaultJson.decode(it) }
    set(value) {
      FILE_SYSTEM.sink(FILE_PATH.toPath()).buffer().use { DefaultJson.decode(value, it) }
    }

  @AfterTest
  fun cleanUp() {
    FILE_SYSTEM.delete(FILE_PATH.toPath())
  }

  @Test
  fun testEncode() = runTest {
    codec.encode(listOf(MYLO))
    val expect: List<Pet> = listOf(MYLO)
    val actual: List<Pet>? = stored
    assertEquals(expect, actual)
  }

  @Test
  fun testDecode() = runTest {
    stored = listOf(OREO)
    val expect: List<Pet> = listOf(OREO)
    val actual: List<Pet>? = codec.decode()
    assertEquals(expect, actual)
  }

  @Test
  fun testTransactionalEncode() = runTest {
    codec.encode(listOf(MYLO))

    // Encoder will fail half way through
    assertFailsWith<NotImplementedError> { codec.encode(listOf(MYLO, KAT)) }

    // The original file should not be touched
    val actual: List<Pet>? = codec.decode()
    assertEquals(listOf(MYLO), actual)
  }

  @Test
  fun testDecodeMalformedFile() = runTest {
    FILE_SYSTEM.sink(FILE_PATH.toPath()).buffer().use { it.writeUtf8("💩") }
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
