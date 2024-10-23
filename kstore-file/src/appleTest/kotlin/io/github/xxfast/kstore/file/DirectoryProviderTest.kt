package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

private val FILE_REGEX = Regex("""
  /Users/.+/Documents
  """.trimIndent()
)

private val CACHES_REGEX = Regex("""
  /Users/.+/Caches
  """.trimIndent()
)

@OptIn(ExperimentalKStoreApi::class, ExperimentalNativeApi::class)
class DirectoryProviders {
  @Test
  fun testDefaultDirectory() {
    val provider: DirectoryProvider = DefaultDirectories
    val files = provider.files.toString()
    val caches= provider.caches.toString()

    assert(files.matches(FILE_REGEX))  { "$files doesn't match expected" }
    assert(caches.matches(CACHES_REGEX))  { "$caches doesn't match expected" }
  }
}
