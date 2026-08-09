package io.github.xxfast.kstore

/**
 * A factory for reusing and managing KStore instances keyed by an arbitrary identity (e.g., file path).
 */
internal object KStoreFactory {
  private val storeMap: MutableMap<Any, KStore<*>> = mutableMapOf()
  private val referenceCountMap: MutableMap<Any, Int> = mutableMapOf()

  /**
   * Get or create a `KStore<T>` for the given key.
   * Ensures that only one instance exists per key.
   *
   * @param key Unique identity to associate the store with (e.g., file path or custom string).
   * @param creator Lambda to create a new store if one doesn't exist for the key.
   */
  @Suppress("UNCHECKED_CAST")
  internal fun <T : Any> getOrCreate(
    key: Any,
    creator: () -> KStore<T>
  ): KStore<T> {
    referenceCountMap[key] = referenceCountMap.getOrElse(key, { 0 }) + 1

    val existing = storeMap[key]
    if (existing != null) {
      return existing as KStore<T>
    }

    val created = creator()
    storeMap[key] = created
    return created
  }

  /**
   * Explicitly remove a store instance for a key (e.g., to free memory).
   */
  internal fun release(key: Any)  {
    val referenceCount = referenceCountMap.getOrElse(key, { 0 })
    if (referenceCount <= 1) {
      storeMap.remove(key)
      referenceCountMap.remove(key)
    } else {
      referenceCountMap[key] = referenceCount - 1
    }
  }
}
