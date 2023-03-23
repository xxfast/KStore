@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast.kstore

import app.cash.turbine.test
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class KStoreTests {
  private val store: KStore<Cat> = KStore(codec = TestCodec())

  @AfterTest
  fun cleanup() {
    stored = null
  }

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadDefault() = runTest {
    val defaultStore: KStore<Pet> = KStore(codec = TestCodec(), default = MYLO)
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadGeneric() = runTest {
    val genericStore: KStore<Kennel<Cat>> = KStore(codec = TestCodec())
    val expect: Kennel<Cat>? = null
    val actual: Kennel<Cat>? = genericStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadGenericDefault() = runTest {
    val genericStore: KStore<Kennel<Cat>> = KStore(codec = TestCodec(), default = Kennel(MYLO))
    val expect: Kennel<Cat> = Kennel(MYLO)
    val actual: Kennel<Cat>? = genericStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadPreviouslyStoredWithDefault() = runTest {
    stored = OREO
    // Mylo will never be sent 😿 because there is already a stored value
    val defaultStore: KStore<Cat> = KStore(codec = TestCodec(), default = MYLO)
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
    val genericStore: KStore<Kennel<Cat>> = KStore(codec = TestCodec())
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
    stored = OREO
    val newStore: KStore<Cat> = KStore(codec = TestCodec())
    newStore.updates.test {
      assertEquals(OREO, awaitItem())
    }
  }

  @Test
  fun testUpdatesWithPreviouslyStoredValueWithDefault() = runTest {
    stored = OREO
    // Mylo will never be sent 😿 because there is already a stored value
    val newStore: KStore<Cat> = KStore(codec = TestCodec(), default = MYLO)
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
    val defaultStore: KStore<Pet> = KStore(codec = TestCodec(), default = MYLO)
    defaultStore.set(OREO)
    defaultStore.reset()
    val expect: Pet = MYLO
    val actual: Pet? = defaultStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testCaching() = runTest {
    store.set(MYLO)
    stored = null
    val expect: Pet = MYLO
    val actual: Pet? = store.get()
    assertSame(expect, actual) // It must be the same *reference*
  }

  @Test
  fun testNonCaching() = runTest {
    val nonCachingStore: KStore<Pet> = KStore(codec = TestCodec(), enableCache = false)
    nonCachingStore.set(MYLO)
    stored = null
    val expect: Pet? = null
    val actual: Pet? = nonCachingStore.get()
    assertEquals(expect, actual)
  }

  @Test
  fun testConcurrentWrite() = runTest {
    val slowStoreForMylo: KStore<Pet> = KStore(
      codec = TestCodec { pet -> if (pet == MYLO) delay(100) }
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
    val slowStoreForOreo: KStore<Pet> = KStore(
      // Oreo always stay out the longest
      codec = TestCodec { pet -> if (pet == OREO) delay(100) }
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
