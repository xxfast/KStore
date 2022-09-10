@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)

package io.github.xxfast

import app.cash.turbine.test
import io.github.xxfast.PetType.Cat
import io.github.xxfast.utils.FILE_SYSTEM
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
import kotlin.test.assertSame

@Serializable
data class Pet(
  val name: String,
  val age: Int,
  val type: PetType
)

enum class PetType { Cat, Dog }

private val MYLO = Pet(name = "Mylo", age = 1, type = Cat)
private val OREO = Pet(name = "Oreo", age = 1, type = Cat)

class KStoreTests {
  private val filePath: String = "test.json"
  private val store: KStore<Pet> = store(filePath = filePath)

  @AfterTest
  fun setup() {
    FILE_SYSTEM.delete(filePath.toPath())
  }

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadDefault() = runTest {
    val defaultStore: KStore<Pet> = store(filePath = filePath, default = MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testWrite() = runTest {
    store.set(MYLO)
    val actual: Pet? = store.get()
    val expect: Pet = MYLO
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
    FILE_SYSTEM.sink(filePath.toPath()).buffer().use { Json.encodeToBufferedSink(OREO, it) }
    val newStore: KStore<Pet> = store(filePath = filePath)
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
    val defaultStore: KStore<Pet> = store(filePath = filePath, default = MYLO)
    defaultStore.set(OREO)
    defaultStore.reset()
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testCaching() = runTest {
    store.set(MYLO)
    FILE_SYSTEM.delete(filePath.toPath())
    val expect: Pet = MYLO
    val actual: Pet? = store.get()
    assertSame(expect, actual) // It must be the same *reference*
  }

  @Test
  fun testNonCaching() = runTest {
    val nonCachingStore: KStore<Pet> = store(enableCache = false, filePath = filePath)
    nonCachingStore.set(MYLO)
    FILE_SYSTEM.delete(filePath.toPath())
    val expect: Pet? = null
    val actual: Pet? = nonCachingStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testConcurrentWrite() = runTest {
    val path: Path = filePath.toPath()
    val slowStoreForMylo: KStore<Pet> = KStore(
      path = path,
      encoder = { value: Pet? ->
        if (value == MYLO) delay(100) // Mylo usually takes his time
        FILE_SYSTEM.sink(path).buffer().use { Json.encodeToBufferedSink(value, it) }
      },
      decoder = { Json.decodeFromBufferedSource(FILE_SYSTEM.source(path).buffer()) }
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
    val path: Path = filePath.toPath()
    val slowStoreForOreo: KStore<Pet> = KStore(
      path = path,
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
}
