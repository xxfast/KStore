@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xxfast.kstore

import app.cash.turbine.test
import io.github.xxfast.kstore.folder.KFolder
import io.github.xxfast.kstore.folder.folderOf
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKStoreApi::class)
class KFolderTests {
  private val folderPath: String = "test"
  private val folder: KFolder<Cat> = folderOf(folderPath) { cat -> cat.json }

  private val Cat.json: String get() = "${name}.json"

  @AfterTest
  fun shutdown() {
    FILE_SYSTEM.deleteRecursively(folderPath.toPath(), false)
  }

  @Test
  fun testEmptyIndex() = runTest {
    val expect: Set<String> = emptySet()
    val actual: Set<String> = folder.indexes
    assertEquals(expect, actual)
  }

  @Test
  fun testIndex() = runTest {
    folder.add(MYLO)
    folder.add(OREO)
    val expect: Set<String> = setOf(MYLO.json, OREO.json)
    val actual: Set<String> = folder.indexes
    assertEquals(expect, actual)
  }

  @Test
  fun testGet() = runTest {
    folder.add(MYLO)
    val expect: Cat = MYLO
    val actual: Cat? = folder.get(MYLO.json)
    assertEquals(expect, actual)
  }

  @Test
  fun testAdd() = runTest {
    folder.add(OREO)
    folder.add(MYLO)
    val expect: Set<String> = setOf(OREO.json, MYLO.json)
    val actual: Set<String> = folder.indexes
    assertEquals(expect, actual)
  }

  @Test
  fun testRemove() = runTest {
    folder.add(MYLO)
    folder.add(OREO)
    folder.remove(OREO.json)
    val expect: Set<String> = setOf(MYLO.json)
    val actual: Set<String> = folder.indexes
    assertEquals(expect, actual)
  }

  @Test
  fun testDelete() = runTest {
    folder.add(MYLO)
    folder.add(OREO)
    folder.delete()
    val expect: Set<String> = emptySet()
    val actual: Set<String> = folder.indexes
    assertEquals(expect, actual)
  }

  @Test
  fun testIndexUpdates() = runTest {
    folder.indexUpdates.test {
      val expectEmptyUpdate: Set<String> = emptySet()
      val actualEmptyUpdate: Set<String> = awaitItem()
      assertEquals(expectEmptyUpdate, actualEmptyUpdate)

      folder.add(MYLO)
      val expectJustMyloUpdate: Set<String> = setOf(MYLO.json)
      val actualJustMyloUpdate: Set<String> = awaitItem()
      assertEquals(expectJustMyloUpdate, actualJustMyloUpdate)

      folder.add(OREO)
      val expectMyloAndOreoUpdate: Set<String> = setOf(MYLO.json, OREO.json)
      val actualMyloAndOreoUpdate: Set<String> = awaitItem()
      assertEquals(expectMyloAndOreoUpdate, actualMyloAndOreoUpdate)

      folder.delete()
      val expectDeletedUpdate: Set<String> = emptySet()
      val actualDeletedUpdate: Set<String> = awaitItem()
      assertEquals(expectDeletedUpdate, actualDeletedUpdate)
    }
  }
}
