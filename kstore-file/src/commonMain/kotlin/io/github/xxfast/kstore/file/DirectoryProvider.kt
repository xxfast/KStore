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
