package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.Cat
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.MYLO
import io.github.xxfast.kstore.storeOf
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKStoreApi::class)
class KCachedStoreTests {
  private val filePath: String = "test_cached.json"
  private val store: KStore<Cat> = storeOf(filePath)

  @AfterTest
  fun setup() {
    FILE_SYSTEM.delete(filePath.toPath())
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
    val nonCachingStore: KStore<Cat> = storeOf(enableCache = false, filePath = filePath)
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
