package io.github.xxfast.kstore.storage

import io.github.xxfast.kstore.DefaultJson
import kotlinx.browser.localStorage
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StorageCodecTests {
  private val codec: StorageCodec<Cat> = StorageCodec(key = KEY)

  private var stored: Cat?
    get() = localStorage[KEY]?.let { DefaultJson.decodeFromString(it) }
    set(value) {
      if(value != null) { localStorage[KEY] = DefaultJson.encodeToString(value) }
      else localStorage.clear()
    }

  @AfterTest
  fun cleanUp() {
    localStorage.clear()
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
    localStorage[KEY] = "💩"
    assertFailsWith<SerializationException> { codec.decode() }
  }
}
