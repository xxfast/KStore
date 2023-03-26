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

import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as decode

class FileCodecTests {
  private val codec: FileCodec<Cat> = FileCodec(filePath = FILE)

  @OptIn(ExperimentalSerializationApi::class)
  private var stored: Cat?
    get() = FILE_SYSTEM.source(FILE.toPath()).buffer().use { DefaultJson.decode(it) }
    set(value) {
      FILE_SYSTEM.sink(FILE.toPath()).buffer().use { DefaultJson.decode(value, it) }
    }

  @AfterTest
  fun cleanUp() {
    FILE_SYSTEM.delete(FILE.toPath())
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
  fun testEncodeDecodeFromDirectory() = runTest {
    val dirCodec: FileCodec<Cat> = FileCodec(filePath = "$FOLDER/$FILE")
    dirCodec.encode(MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = dirCodec.decode()
    assertEquals(expect, actual)
  }

  @Test
  fun testDecodeMalformedFile() = runTest {
    FILE_SYSTEM.sink(FILE.toPath()).buffer().use { it.writeUtf8("💩") }
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
