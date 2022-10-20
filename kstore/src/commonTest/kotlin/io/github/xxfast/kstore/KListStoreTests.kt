@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast.kstore

import app.cash.turbine.test
import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KListStoreTests {
  private val filePath: String = "build/test_lists.json"
  private val store: KStore<List<Cat>> = listStoreOf(filePath = filePath)

  @AfterTest
  fun setup() {
    FILE_SYSTEM.delete(filePath.toPath())
  }

  @Test
  fun testReadEmpty() = runTest {
    val expect: List<Cat> = emptyList()
    val actual: List<Cat> = store.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testReadIndex() = runTest {
    store.plus(OREO, MYLO)
    val expect: Cat = MYLO
    val actual: Cat? = store.get(1)
    assertEquals(expect, actual)
  }

  @Test
  fun testReadDefault() = runTest {
    val defaultStore: KStore<List<Cat>> = listStoreOf(filePath = filePath, default = listOf(MYLO))
    val expect: List<Cat> = listOf(MYLO)
    val actual: List<Cat> = defaultStore.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testPlus() = runTest {
    store.plus(OREO)
    val expect: List<Cat> = listOf(OREO)
    val actual: List<Cat> = store.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testMinus() = runTest {
    store.plus(MYLO)
    store.minus(OREO)
    val expect: List<Cat> = listOf(MYLO)
    val actual: List<Cat> = store.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testMap() = runTest {
    store.plus(OREO)
    store.map { cat -> cat.copy(age = cat.age + 1) }
    val expect: List<Cat> = listOf(OREO.copy(age = OREO.age + 1))
    val actual: List<Cat> = store.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testMapIndexed() = runTest {
    store.plus(MYLO)
    store.plus(OREO)
    store.mapIndexed { index, cat -> cat.copy(age = cat.age + index) }
    val expect: List<Cat> = listOf(MYLO, OREO.copy(age = OREO.age + 1))
    val actual: List<Cat> = store.getOrEmpty()
    assertEquals(expect, actual)
  }

  @Test
  fun testUpdatesOrEmpty() = runTest {
    store.updatesOrEmpty.test {
      assertEquals(mutableListOf(), awaitItem())
      store.plus(MYLO)
      val justMylo: List<Cat> = awaitItem()
      assertEquals(listOf(MYLO), justMylo)
      store.plus(OREO)
      val myloAndOreo: List<Cat> = awaitItem()
      assertEquals(listOf(MYLO, OREO), myloAndOreo)
      store.minus(OREO)
      val justMyloAgain: List<Cat> = awaitItem()
      assertEquals(listOf(MYLO), justMyloAgain)
    }
  }
}
