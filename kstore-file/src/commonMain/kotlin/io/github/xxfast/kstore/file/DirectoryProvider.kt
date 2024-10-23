package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.io.files.Path

@ExperimentalKStoreApi
public expect val DefaultDirectories: DirectoryProvider

@ExperimentalKStoreApi
public interface DirectoryProvider {
  public val files: Path
  public val caches: Path
}

@ExperimentalKStoreApi
public fun DirectoryProvider(
  files: Path,
  caches: Path
): DirectoryProvider = object : DirectoryProvider {
  override val files: Path get() = files
  override val caches: Path get() = caches
}
