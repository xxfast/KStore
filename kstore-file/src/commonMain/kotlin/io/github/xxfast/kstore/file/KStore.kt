package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Creates a store with [FileCodec]
 *
 * @param file path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param json JSON Serializer to use. defaults to [DefaultJson]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  file: Path,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = DefaultJson,
): KStore<T> = storeOf(
  codec = FileCodec(file = file, json = json),
  default = default,
  enableCache = enableCache,
)

/**
 * Creates a store with a given codec
 *
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param codec codec to be used.
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  codec: Codec<T>,
  default: T? = null,
  enableCache: Boolean = true,
): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = codec
)

