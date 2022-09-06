@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast

import io.github.xxfast.PetType.Cat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertSame

@Serializable
data class Pet(
  val name: String,
  val age: Int,
  val type: PetType
)

enum class PetType { Cat, Dog }

class ValueStoreTests {
  private val store: ValueStore<Pet> = ValueStore(path = "test.json".toPath())

  @Test
  fun testReadEmpty() = runTest {
    val expect: Pet? = null
    val actual: Pet? = store.get<Pet>()
    assertSame(expect, actual)
  }

  @Test
  fun testWriteValue() = runTest {
    val mylo = Pet("Mylo", 1, Cat)
    store.set(mylo)
    val actual: Pet? = store.get<Pet>()
    val expect: Pet = mylo
    assertSame(expect, actual)
  }
}
