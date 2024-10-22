package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider = AppleDirectoryProvider(NSFileManager.defaultManager)

@ExperimentalKStoreApi
private class AppleDirectoryProvider(private val fileManager: NSFileManager): DirectoryProvider {
  @OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
  override val files: Path get() {
    val url: NSURL? = fileManager.URLForDirectory(
      directory = NSDocumentDirectory,
      appropriateForURL = null,
      create = false,
      inDomain = NSUserDomainMask,
      error = null
    )

    val urlPath: String = requireNotNull(url?.path) {
      "Unable to locate or optionally create documents"
    }

    val path = Path(urlPath)
    if (!SystemFileSystem.exists(path)) SystemFileSystem.createDirectories(path, true)
    return path
  }

  @OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
  override val caches: Path get() {
    val url: NSURL? = fileManager.URLForDirectory(
      directory = NSCachesDirectory,
      appropriateForURL = null,
      create = false,
      inDomain = NSUserDomainMask,
      error = null
    )

    val urlPath: String = requireNotNull(url?.path) {
      "Unable to locate or optionally create caches"
    }

    val path = Path(urlPath)
    if (!SystemFileSystem.exists(path)) SystemFileSystem.createDirectories(path, true)
    return path
  }
}
