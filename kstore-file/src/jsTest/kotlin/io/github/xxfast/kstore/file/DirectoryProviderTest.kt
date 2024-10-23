package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalKStoreApi::class)
class DirectoryProviders {
  @Test
  fun testDefaultDirectory() {
    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()

    // Node doesn't have a default directory, so we can't check the exact path
    assertTrue("$files is empty") { files.isNotEmpty() }
    assertTrue("$caches is empty") { caches.isNotEmpty() }
  }
}
