@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast

import io.github.xxfast.PetType.Cat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
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

class KStoreTests {
  private val store: KStore<Pet> = KStore(path = "test.json".toPath())

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get<Pet>()
    assertSame(expect, actual)
  }

  @Test
  fun testWriteValue() = runTest {
    val mylo = Pet(name = "Mylo", age = 1, type = Cat)
    store.set(mylo)
    val actual: Pet? = store.get<Pet>()
    val expect: Pet = mylo
    assertEquals(expect, actual)
  }
}
