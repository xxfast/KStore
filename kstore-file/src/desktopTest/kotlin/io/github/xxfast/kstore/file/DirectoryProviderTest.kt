package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlin.test.Test

/**
 * desktop tests are run on linux agents, so the paths are linux specific.
 */
private val FILE_REGEX = Regex("""
  .+/io.github.xxfast.kstore
  """.trimIndent()
)

private val CACHES_REGEX = Regex("""
  .+/io.github.xxfast.kstore
  """.trimIndent()
)

@OptIn(ExperimentalKStoreApi::class)
class DirectoryProviders {
  @Test
  fun testDefaultDirectory() {
    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()
    assert(files.matches(FILE_REGEX)) { "$files doesn't match expected" }
    assert(caches.matches(CACHES_REGEX)) { "$caches doesn't match expected" }
  }
}
