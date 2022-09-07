@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast

import app.cash.turbine.test
import io.github.xxfast.PetType.Cat
import io.github.xxfast.utils.FILE_SYSTEM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import kotlin.test.*

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
  private val path: Path = "test.json".toPath()
  private val store: KStore<Pet> = KStore(path = path)

  @AfterTest
  fun setup(){
    FILE_SYSTEM.delete(path)
  }

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get<Pet>()
    assertSame(expect, actual)
  }

  @Test
  fun testWrite() = runTest {
    store.set(MYLO)
    val actual: Pet? = store.get<Pet>()
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
  fun testClear() = runTest {
    store.set(MYLO)
    store.clear()
    val expect: Pet? = null
    val actual: Pet? = store.get<Pet>()
    assertSame(expect, actual)
  }

  @Test
  fun testCaching() = runTest {
    store.set(MYLO)
    FILE_SYSTEM.delete(path)
    val expect: Pet = MYLO
    val actual: Pet? = store.get<Pet>()
    assertSame(expect, actual)
  }

  @Test
  fun testNonCaching() = runTest {
    val nonCachingStore = KStore<Pet>(enableCache = false, path = path)
    nonCachingStore.set(MYLO)
    FILE_SYSTEM.delete(path)
    val expect: Pet? = null
    val actual: Pet? = nonCachingStore.get<Pet>()
    assertSame(expect, actual)
  }
}
