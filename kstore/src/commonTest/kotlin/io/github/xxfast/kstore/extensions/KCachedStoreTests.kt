package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.Cat
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.MYLO
import io.github.xxfast.kstore.TestCodec
import io.github.xxfast.kstore.stored
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKStoreApi::class)
class KCachedStoreTests {
  private val store: KStore<Cat> = KStore(codec = TestCodec())

  @AfterTest
  fun setup() {
    stored = null
  }

  @Test
  fun testCachedEmpty() = runTest {
    val expect: Cat? = null
    val actual: Cat? = store.cached
    assertEquals(expect, actual)
  }

  @Test
  fun testCachedNotEmpty() = runTest {
    store.set(MYLO)
    val expect: Cat = MYLO
    val actual: Cat? = store.cached
    assertEquals(expect, actual)
  }

  @Test
  fun testCachedWhenCacheDisabled() = runTest {
    val nonCachingStore: KStore<Cat> = KStore(enableCache = false, codec = TestCodec())
    nonCachingStore.set(MYLO)
    val expect: Cat = MYLO
    val actual: Cat? = nonCachingStore.cached
    assertEquals(expect, actual)
  }

  @Test
  fun testCachedCleared() = runTest {
    store.set(MYLO)
    store.delete()
    val expect: Cat? = null
    val actual: Cat? = store.cached
    assertEquals(expect, actual)
  }
}
