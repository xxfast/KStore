package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Creates a store that contains a list
 *
 * @param file path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to empty list
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param json Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
 *
 * @return store that contains a list of type [T]
 */
public inline fun <reified T : @Serializable Any> listStoreOf(
  file: Path,
  default: List<T> = emptyList(),
  enableCache: Boolean = true,
  json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
): KStore<List<T>> = storeOf(file, default, enableCache, json)