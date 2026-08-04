package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
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
    get() = if (!SystemFileSystem.exists(Path(FILE_PATH))) null
    else {
      SystemFileSystem.source(Path(FILE_PATH))
        .buffered()
        .use { DefaultJson.decodeFromSource(it) }
    }

    set(value) {
      SystemFileSystem.sink(Path(FILE_PATH))
        .buffered()
        .use { DefaultJson.encodeToSink(value, it) }
    }

  @AfterTest
  fun cleanUp() {
    SystemFileSystem.delete(Path(FILE_PATH), false)
    SystemFileSystem.delete(Path("$FILE_PATH.temp"), false)
  }

  @Test
  fun testEncode() = runTest {
    codec.encode(listOf(MYLO))
    val expect: List<Pet> = listOf(MYLO)
    val actual: List<Pet>? = stored
    assertEquals(expect, actual)
  }

  @Test
  fun testEncodeWithNullValue() = runTest {
    codec.encode(null)
    val expect: List<Pet>? = null
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

  @Test
  fun testMoveFallsBackToCopyWhenAtomicMoveUnsupported() = runTest {
    val tempFile = Path("$FILE_PATH.temp")
    SystemFileSystem.sink(tempFile).buffered().use { DefaultJson.encodeToSink<List<Pet>>(listOf(MYLO), it) }

    // Simulate platforms (e.g. Android 7 and below) where atomicMove throws - see issue #137
    moveOrCopy(
      source = tempFile,
      destination = Path(FILE_PATH),
      atomicMove = { _, _ -> throw UnsupportedOperationException("Atomic move not supported") },
    )

    // The value is persisted via the copy-and-delete fallback ...
    assertEquals(listOf(MYLO), stored)
    // ... and the temp file is cleaned up afterwards.
    assertEquals(false, SystemFileSystem.exists(tempFile))
  }

  @Test
  fun testMoveDeletesCorruptDestinationWhenCopyFails() = runTest {
    val tempFile = Path("$FILE_PATH.temp") // intentionally never created, so the fallback copy fails
    SystemFileSystem.sink(Path(FILE_PATH)).buffered().use { it.writeString("stale") } // pre-existing destination

    assertFailsWith<FileNotFoundException> {
      moveOrCopy(
        source = tempFile,
        destination = Path(FILE_PATH),
        atomicMove = { _, _ -> throw UnsupportedOperationException("Atomic move not supported") },
      )
    }

    // The possibly-corrupt destination is removed rather than left half-written.
    assertEquals(false, SystemFileSystem.exists(Path(FILE_PATH)))
  }
}
