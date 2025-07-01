package io.github.xxfast.kstore.storage

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer

public inline fun <reified T : @Serializable Any> StorageCodec(
  key: String,
  format: StringFormat = DefaultJson,
  storage: Storage = localStorage,
): StorageCodec<T> = StorageCodec(
  key = key,
  format = format,
  serializer = format.serializersModule.serializer(),
  storage = storage,
)

public class StorageCodec<T : @Serializable Any>(
  private val key: String,
  private val format: StringFormat,
  private val serializer: KSerializer<T>,
  private val storage: Storage,
) : Codec<T> {
  override suspend fun encode(value: T?) {
    if (value != null) storage[key] = format.encodeToString(serializer, value)
    else storage.remove(key)
  }

  override suspend fun decode(): T? = storage[key]
    ?.let { format.decodeFromString(serializer, it) }

  override fun id(): Any {
    return storage.toString() + serializer.descriptor.serialName
  }
}
