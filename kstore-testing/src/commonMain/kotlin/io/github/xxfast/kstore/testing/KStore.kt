package io.github.xxfast.kstore.testing

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat

/**
 * Creates a store with [InMemoryCodec]
 *
 * @param default returns this value if the record is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from storage
 * @param format Serializer to use. defaults to [DefaultJson]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> testStoreOf(
  default: T? = null,
  enableCache: Boolean = true,
  format: StringFormat = DefaultJson,
): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = InMemoryCodec(format)
)
