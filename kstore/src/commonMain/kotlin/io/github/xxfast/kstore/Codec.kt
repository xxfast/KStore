package io.github.xxfast.kstore

import kotlinx.serialization.Serializable

/**
 * Encoding and decoding behavior that is used by the store
 */
public interface Codec<T: @Serializable Any> {
  /**
   * Tells the store how to encode an given value
   * @param value optional value to encode
   */
  public suspend fun encode(value: T?)

  /**
   * Tells the store how to decode an given value
   * @return optional value that is decoded
   */
  public suspend fun decode(): T?
}
