package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.serialization.Serializable

/**
 * Get the cached value synchronously.
 * Note: This value will be set even when the store's [KStore.enableCache] is set to false.
 * Note: This value can be null if
 *   1. no previous calls to [KStore.get]
 *   2. there's no active subscriber to [KStore.updates]
 */
@ExperimentalKStoreApi
public val <T: @Serializable Any> KStore<T>.cached: T? get() = cache.value
