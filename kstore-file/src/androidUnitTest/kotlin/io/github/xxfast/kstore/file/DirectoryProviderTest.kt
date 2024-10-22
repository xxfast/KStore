package io.github.xxfast.kstore.file

import android.content.Context
import androidx.startup.AppInitializer
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalKStoreApi::class)
class DirectoryProviders {

  @AfterTest
  fun cleanUp() {
    _directories = RootDirectoryProvider
  }

  @Test
  fun testRootDirectoryWhenNotInitialised() {
    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()
    assert(files == "/") { "$files doesn't match expected" }
    assert(caches == "/") { "$caches doesn't match expected" }
  }

  @Test
  fun testRootDirectoryWhenInitialised() {
    val context: Context = mockk()

    every { context.applicationContext } returns context
    every { context.filesDir } returns File("files")
    every { context.cacheDir } returns File("caches")

    AppInitializer.getInstance(context)
      .initializeComponent(DirectoriesInitializer::class.java)

    val provider: DirectoryProvider = DefaultDirectories
    val files: String = provider.files.toString()
    val caches: String = provider.caches.toString()
    assert(files == "files") { "$files doesn't match expected" }
    assert(caches == "caches") { "$caches doesn't match expected" }
  }
}
