package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.io.files.Path

@ExperimentalKStoreApi
public actual val DefaultDirectories: DirectoryProvider = JsDirectoryProvider()

@ExperimentalKStoreApi
public class JsDirectoryProvider: DirectoryProvider {
  private val os: Os by lazy {
    try { js("eval('require')('os')") }
    catch (e: Throwable) { throw UnsupportedOperationException("Module 'os' could not be imported", e) }
  }

  public override val files: Path get() = Path(os.homedir())
  public override val caches: Path = Path(os.tmpdir())
}
