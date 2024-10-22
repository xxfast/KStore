package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

@OptIn(ExperimentalKStoreApi::class)
class DirectoryProviders {
  @OptIn(ExperimentalNativeApi::class)
  @Test
  fun testDefaultDirectory() {
    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()
    assert(files.isNotEmpty()) { "$files is empty" }
    assert(caches.isNotEmpty()) { "$caches is empty" }
  }
}
