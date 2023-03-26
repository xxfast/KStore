package io.github.xxfast.kstore

import kotlinx.serialization.Serializable

var stored: @Serializable Any? = null

class TestCodec<T : @Serializable Any>(
  private val delay: (suspend (value: T?) -> Unit)? = null,
) : Codec<T> {
  override suspend fun encode(value: T?) {
    delay?.invoke(value)
    stored = value
  }

  @Suppress("UNCHECKED_CAST")
  override suspend fun decode(): T? {
    val value: T? = stored as? T
    delay?.invoke(value)
    return value
  }
}
