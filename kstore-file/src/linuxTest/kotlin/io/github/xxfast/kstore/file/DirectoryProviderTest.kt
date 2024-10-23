package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlin.experimental.ExperimentalNativeApi

private val FILE_REGEX = Regex("""
  /Users/.+
  """.trimIndent()
)

private val CACHES_REGEX = Regex("""
  /var/folders/.+/T
  """.trimIndent()
)

@OptIn(ExperimentalKStoreApi::class)
class DirectoryProviders {
  /**
   * TODO: Fix this test, we are getting a runtime exception:
   * kotlin.native.internal.FileFailedToInitializeException: There was an error during file or class initialization
   */
  // @Test
  @OptIn(ExperimentalNativeApi::class)
  fun testDefaultDirectory() {
    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()
    assert(files.matches(FILE_REGEX)) { "$files doesn't match expected" }
    assert(caches.matches(CACHES_REGEX)) { "$caches doesn't match expected" }
  }
}
