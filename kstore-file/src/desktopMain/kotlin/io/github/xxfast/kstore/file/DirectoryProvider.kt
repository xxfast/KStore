package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider = DesktopDirectoryProvider(
  packageName = "io.github.xxfast.kstore",
  version = "",
  author = "xxfast"
)

@ExperimentalKStoreApi
private class DesktopDirectoryProvider(
  private val packageName: String,
  private val version: String,
  private val author: String,
) : DirectoryProvider {
  private val appDirs: AppDirs = AppDirsFactory.getInstance()

  override val files: Path get() {
    val filesDir: String = appDirs.getUserDataDir(packageName, version, author)
    val path = Path(filesDir)
    SystemFileSystem.createDirectories(path)
    return path
  }

  override val caches: Path get() {
    val filesDir: String = appDirs.getUserCacheDir(packageName, version, author)
    val path = Path(filesDir)
    SystemFileSystem.createDirectories(path)
    return path
  }
}