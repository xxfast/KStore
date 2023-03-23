package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Creates a store with [FileCodec]
 *
 * @param filePath path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param json Serializer to use. defaults to [DefaultJson]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  filePath: String,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = DefaultJson,
): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = FileCodec(filePath, json)
)
