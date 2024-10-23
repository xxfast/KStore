package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.getenv

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider = LinuxDirectoryProvider()

@ExperimentalKStoreApi
private class LinuxDirectoryProvider: DirectoryProvider {

  @OptIn(ExperimentalForeignApi::class)
  private fun env(vararg names: String) = names
    .firstNotNullOf { getenv(it) }
    .toKString()

  override val files: Path = Path(env("HOME"))
  override val caches: Path = Path(env("TMPDIR", "TMP"))
}
