@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)

package io.github.xxfast.kstore

import app.cash.turbine.test
import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@Serializable
sealed class Pet {
  abstract val name: String
  abstract val age: Int
}

@Serializable
data class Cat(
  override val name: String,
  override val age: Int,
  val lives: Int = 9,
) : Pet()

@Serializable
data class Kennel<T : Pet>(val pet: T)

internal val MYLO = Cat(name = "Mylo", age = 1)
internal val OREO = Cat(name = "Oreo", age = 1)

private const val FILE = "test.json"
private const val FOLDER = "build/bin/tests"

class KStoreTests {
  private val store: KStore<Cat> = storeOf(filePath = FILE)

  @AfterTest
  fun cleanup() {
    FILE_SYSTEM.delete(FILE.toPath())
    FILE_SYSTEM.deleteRecursively(FOLDER.toPath())
  }

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadDefault() = runTest {
    val defaultStore: KStore<Pet> = storeOf(filePath = FILE, default = MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadGeneric() = runTest {
    val genericStore: KStore<Kennel<Cat>> = storeOf(filePath = FILE)
    val expect: Kennel<Cat>? = null
    val actual: Kennel<Cat>? = genericStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadGenericDefault() = runTest {
    val genericStore: KStore<Kennel<Cat>> = storeOf(filePath = FILE, Kennel(MYLO))
    val expect: Kennel<Cat> = Kennel(MYLO)
    val actual: Kennel<Cat>? = genericStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadPreviouslyStoredWithDefault() = runTest {
    FILE_SYSTEM.sink(FILE.toPath()).buffer().use { Json.encodeToBufferedSink(OREO, it) }
    // Mylo will never be sent 😿 because there is already a stored value
    val defaultStore: KStore<Cat> = storeOf(filePath = FILE, default = MYLO)
    val expect: Pet = OREO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testWrite() = runTest {
    store.set(MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = store.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testWriteGeneric() = runTest {
    val genericStore: KStore<Kennel<Cat>> = storeOf(filePath = FILE)
    genericStore.set(Kennel(MYLO))
    val expect: Kennel<Cat> = Kennel(MYLO)
    val actual: Kennel<Cat>? = genericStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testUpdates() = runTest {
    store.updates.test {
      assertEquals(null, awaitItem())
      store.set(MYLO)
      assertEquals(MYLO, awaitItem())
      store.set(OREO)
      assertEquals(OREO, awaitItem())
    }
  }

  @Test
  fun testUpdatesWithPreviouslyStoredValue() = runTest {
    FILE_SYSTEM.sink(FILE.toPath()).buffer().use { Json.encodeToBufferedSink(OREO, it) }
    val newStore: KStore<Cat> = storeOf(filePath = FILE)
    newStore.updates.test {
      assertEquals(OREO, awaitItem())
    }
  }

  @Test
  fun testUpdatesWithPreviouslyStoredValueWithDefault() = runTest {
    FILE_SYSTEM.sink(FILE.toPath()).buffer().use { Json.encodeToBufferedSink(OREO, it) }
    // Mylo will never be sent 😿 because there is already a stored value
    val newStore: KStore<Cat> = storeOf(filePath = FILE, default = MYLO)
    newStore.updates.test {
      assertEquals(OREO, awaitItem())
    }
  }

  @Test
  fun testDelete() = runTest {
    store.set(MYLO)
    store.delete()
    val expect: Pet? = null
    val actual: Pet? = store.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReset() = runTest {
    val defaultStore: KStore<Pet> = storeOf(filePath = FILE, default = MYLO)
    defaultStore.set(OREO)
    defaultStore.reset()
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testCaching() = runTest {
    store.set(MYLO)
    FILE_SYSTEM.delete(FILE.toPath())
    val expect: Pet = MYLO
    val actual: Pet? = store.get()
    assertSame(expect, actual) // It must be the same *reference*
  }

  @Test
  fun testNonCaching() = runTest {
    val nonCachingStore: KStore<Pet> = storeOf(enableCache = false, filePath = FILE)
    nonCachingStore.set(MYLO)
    FILE_SYSTEM.delete(FILE.toPath())
    val expect: Pet? = null
    val actual: Pet? = nonCachingStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testConcurrentWrite() = runTest {
    val path: Path = FILE.toPath()
    val slowStoreForMylo: KStore<Pet> = KStore(
      encoder = { value: Pet? ->
        if (value == MYLO) delay(100) // Mylo usually takes his time
        FILE_SYSTEM.sink(path).buffer().use { Json.encodeToBufferedSink(value, it) }
      },
      decoder = {
        try {Json.decodeFromBufferedSource(FILE_SYSTEM.source(path).buffer()) }
        catch (e: Exception) { null }
      }
    )

    slowStoreForMylo.updates.test {
      assertEquals(null, awaitItem())
      val firstWrite: Deferred<Unit> = async { slowStoreForMylo.set(MYLO) }
      val secondWrite: Deferred<Unit> = async { slowStoreForMylo.set(OREO) }
      awaitAll(firstWrite, secondWrite)
      assertEquals(MYLO, awaitItem())
      assertEquals(OREO, awaitItem())
    }
  }

  @Test
  fun testConcurrentRead() = runTest {
    val path: Path = FILE.toPath()
    val slowStoreForOreo: KStore<Pet> = KStore(
      encoder = { value: Pet? ->
        FILE_SYSTEM.sink(path).buffer().use { Json.encodeToBufferedSink(value, it) }
      },
      decoder = {
        val value: Pet? = Json.decodeFromBufferedSource(FILE_SYSTEM.source(path).buffer())
        if (value == OREO) delay(100) // Oreo always stay out the longest
        return@KStore value
      }
    )

    slowStoreForOreo.set(OREO)
    val first: Pet? = slowStoreForOreo.get()
    slowStoreForOreo.set(MYLO)
    val second: Pet? = slowStoreForOreo.get()

    assertEquals(first, OREO)
    assertEquals(second, MYLO)
  }

  @Test
  fun testUpdate() = runTest {
    store.set(MYLO)
    store.update { value -> value?.copy(age = value.age + 1) } // Happy birthday! 🥳
    val actual: Pet? = store.get()
    val expect: Pet = MYLO.copy(age = MYLO.age + 1)
    assertEquals(actual, expect)
  }

  @Test
  fun testWriteInDirectory() = runTest {
    val storeInDirectory: KStore<Pet> = storeOf(filePath = "$FOLDER/$FILE")
    storeInDirectory.set(MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = storeInDirectory.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadingMalformedFile() = runTest {
    FILE_SYSTEM.sink(FILE.toPath()).buffer().use { it.writeUtf8("💩") }
    assertFailsWith<SerializationException> { store.get() }
  }
}
