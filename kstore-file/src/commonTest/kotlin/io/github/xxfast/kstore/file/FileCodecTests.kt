package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class FileCodecTests {
  private val codec: FileCodec<List<Pet>> = FileCodec(file = Path(FILE_PATH))

  @OptIn(ExperimentalSerializationApi::class)
  private var stored: List<Pet>?
    get() = SystemFileSystem.source(Path(FILE_PATH))
      .buffered()
      .use { DefaultJson.decodeFromSource(it) }

    set(value) {
      SystemFileSystem.sink(Path(FILE_PATH))
        .buffered()
        .use { DefaultJson.encodeToSink(value, it) }
    }

  @AfterTest
  fun cleanUp() {
    SystemFileSystem.delete(Path(FILE_PATH))
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
    SystemFileSystem.sink(Path(FILE_PATH)).buffered().use { it.writeString("💩") }
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
