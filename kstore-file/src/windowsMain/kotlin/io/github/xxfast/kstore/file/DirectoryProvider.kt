package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.getenv

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider get() = WindowsDirectoryProvider()

@ExperimentalKStoreApi
private class WindowsDirectoryProvider : DirectoryProvider {

  @OptIn(ExperimentalForeignApi::class)
  private fun env(vararg names: String) = names
    .find { getenv(it) != null }
    ?.let { name -> getenv(name) }
    ?.toKString()

  override val files: Path = env("CSIDL_LOCAL_APPDATA")
    ?.let { Path(it) }
    ?: Path("C:\\Users")

  override val caches: Path = env("TMPDIR", "TMP")
    ?.let { Path(it) }
    ?: Path("C:\\AppData\\Local\\Temp")
}
