package io.github.xxfast.kstore.storage

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Creates a store with [StorageCodec]
 *
 * @param key key for the record that is managed by this store
 * @param default returns this value if the record is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from storage
 * @param json Serializer to use. defaults to [DefaultJson]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  key: String,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = DefaultJson,
  storage: Storage = localStorage,
): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = StorageCodec(key, json, storage)
)
