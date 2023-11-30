package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.file.utils.FILE_SYSTEM
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.test.*

import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as decode

class FileCodecTests {
  private val codec: FileCodec<Cat> = FileCodec(file = FILE_PATH.toPath())

  @OptIn(ExperimentalSerializationApi::class)
  private var stored: Cat?
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
    FILE_SYSTEM.sink(FILE_PATH.toPath()).buffer().use { it.writeUtf8("💩") }
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
