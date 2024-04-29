package io.github.xxfast.kstore.storage

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

public inline fun <reified T : @Serializable Any> StorageCodec(
  key: String,
  json: Json = DefaultJson,
  storage: Storage = localStorage,
): StorageCodec<T> = StorageCodec(
  key = key,
  json = json,
  serializer = json.serializersModule.serializer(),
  storage = storage,
)

public class StorageCodec<T : @Serializable Any>(
  private val key: String,
  private val json: Json,
  private val serializer: KSerializer<T>,
  private val storage: Storage,
) : Codec<T> {
  override suspend fun encode(value: T?) {
    if (value != null) storage[key] = json.encodeToString(serializer, value)
    else storage.remove(key)
  }

  override suspend fun decode(): T? = storage[key]
    ?.let { json.decodeFromString(serializer, it) }
}
