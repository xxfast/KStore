package io.github.xxfast.kstore

import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileNotFoundException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

/**
 * Creates a store
 *
 * @param filePath path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param serializer Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
 *
 * @return store that contains a value of type [T]
 */
@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T : @Serializable Any> storeOf(
  filePath: String,
  default: T? = null,
  enableCache: Boolean = true,
  serializer: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
): KStore<T> {
  val path: Path = filePath.toPath()

  val encoder: (T?) -> Unit = { value: T? ->
    val parentFolder: Path? = path.parent

    if (parentFolder != null && !FILE_SYSTEM.exists(parentFolder))
      FILE_SYSTEM.createDirectories(parentFolder, mustCreate = false)

    if (value != null) FILE_SYSTEM.sink(path).buffer().use { serializer.encode(value, it) }
    else FILE_SYSTEM.delete(path)
  }

  val decoder: () -> T? = {
    try { serializer.decode(FILE_SYSTEM.source(path).buffer()) }
    catch (e: FileNotFoundException) { null }
  }

  return KStore(default, enableCache, encoder, decoder)
}

/**
 * Creates a store with a custom encoder and a decoder
 *
 * @param default returns this value if the decoder returns null. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from decoder
 * @param encoder lambda to encode the value with
 * @param decoder lambda to decode the value with
 */
public class KStore<T : @Serializable Any>(
  private val default: T? = null,
  private val enableCache: Boolean = true,
  private val encoder: suspend (T?) -> Unit,
  private val decoder: suspend () -> T?,
) {
  private val lock: Mutex = Mutex()
  internal val cache: MutableStateFlow<T?> = MutableStateFlow(default)

  /** Observe store for updates */
  public val updates: Flow<T?> get() = this.cache
    .onStart { read(fromCache = false) } // updates will always start with a fresh read

  private suspend fun write(value: T?) {
    encoder.invoke(value)
    cache.emit(value)
  }

  internal suspend fun read(fromCache: Boolean): T? {
    if (fromCache && cache.value != default) return cache.value
    val decoded: T? = decoder.invoke()
    val emitted: T? = decoded ?: default
    cache.emit(emitted)
    return emitted
  }

  /**
   * Set a value to the store
   *
   * @param value to set
   */
  public suspend fun set(value: T?): Unit = lock.withLock { write(value) }

  /**
   * Get a value from the store
   *
   * @return value stored/cached (if enabled)
   */
  public suspend fun get(): T? = lock.withLock { read(enableCache) }

  /**
   * Update a value in a store.
   * Note: This maintains a single mutex lock for both get and set
   *
   * @param operation lambda to update a given value of type [T]
   */
  public suspend fun update(operation: (T?) -> T?): Unit = lock.withLock {
    val previous: T? = read(enableCache)
    val updated: T? = operation(previous)
    write(updated)
  }

  /**
   * Set the value of the store to null
   */
  public suspend fun delete() {
    set(null)
    cache.emit(null)
  }

  /**
   * Set the value of the store to the default
   */
  public suspend fun reset() {
    set(default)
    cache.emit(default)
  }
}
