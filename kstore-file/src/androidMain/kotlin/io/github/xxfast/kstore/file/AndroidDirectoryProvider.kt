package io.github.xxfast.kstore.file

import android.content.Context
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.io.files.Path

/**
 * When no context is available, this will point to root directory.
 */
@ExperimentalKStoreApi
internal var _directories: DirectoryProvider = RootDirectoryProvider

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider get() = _directories

@ExperimentalKStoreApi
internal class AndroidDirectoryProvider(context: Context) : DirectoryProvider {
  override val files: Path = Path(context.filesDir.path)
  override val caches: Path = Path(context.cacheDir.path)
}

@ExperimentalKStoreApi
internal object RootDirectoryProvider: DirectoryProvider {
  override val files: Path = Path("/")
  override val caches: Path = Path("/")
}
