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
  private val file = Path(FILE_PATH)

  private val codec: FileCodec<Cat> = FileCodec(
    file = file,
    json = DefaultJson,
  )

  @OptIn(ExperimentalSerializationApi::class)
  private var stored: Cat?
    get() = SystemFileSystem.source(file).buffered().use { DefaultJson.decodeFromSource(it) }
    set(value) {
      SystemFileSystem.sink(file).buffered().use { DefaultJson.encodeToSink(value, it) }
    }

  @AfterTest
  fun cleanUp() {
    SystemFileSystem.delete(Path(FILE_PATH))
  }

  @Test
  fun testEncode() = runTest {
    codec.encode(MYLO)
    val expect: Cat = MYLO
    val actual: Cat? = stored
    assertEquals(expect, actual)
  }

  @Test
  fun testDecode() = runTest {
    stored = OREO
    val expect: Cat = OREO
    val actual: Cat? = codec.decode()
    assertEquals(expect, actual)
  }

  @Test
  fun testDecodeMalformedFile() = runTest {
    SystemFileSystem.sink(file).buffered().use { it.writeString("💩") }
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
