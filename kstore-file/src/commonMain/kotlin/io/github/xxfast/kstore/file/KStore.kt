package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.format.KStoreFormat
import io.github.xxfast.kstore.file.format.KStoreFormatJson
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Creates a store with [FileCodec]
 *
 * @param file path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param format Serializer to use. defaults to [KStoreFormatJson]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  file: Path,
  default: T? = null,
  enableCache: Boolean = true,
  format: KStoreFormat<T> = KStoreFormatJson(DefaultJson, DefaultJson.serializersModule.serializer()),
  ): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = FileCodec(file, format)
)
