package io.github.xxfast.kstore

import kotlinx.serialization.Serializable

/**
 * Encoding and decoding behavior that is used by the store
 */
public interface Codec<T: @Serializable Any> {
  public suspend fun encode(value: T?)
  public suspend fun decode(): T?
}
