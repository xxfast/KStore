package io.github.xxfast.kstore

import io.github.xxfast.kstore.utils.StoreDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


/**
 * Creates a store with a custom encoder and a decoder
 *
 * @param default returns this value if the decoder returns null. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from decoder
 * @param codec codec to use to encode/decode a value with/from
 */
public class KStore<T : @Serializable Any>(
  private val default: T? = null,
  private val enableCache: Boolean = true,
  private val codec: Codec<T>,
) {
  private val lock: Mutex = Mutex()
  internal val cache: MutableStateFlow<T?> = MutableStateFlow(default)

  /** Observe store for updates */
  public val updates: Flow<T?>
    get() = this.cache
      .onStart {
        lock.withLock {
          read(fromCache = false)
        }
      } // updates will always start with a fresh read

  private suspend fun write(value: T?): Unit = withContext(StoreDispatcher) {
    codec.encode(value)
    cache.emit(value)
  }

  private suspend fun read(fromCache: Boolean): T? = withContext(StoreDispatcher) {
    if (fromCache && cache.value != default) return@withContext cache.value
    val decoded: T? = codec.decode()
    val emitted: T? = decoded ?: default
    cache.emit(emitted)
    return@withContext emitted
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
