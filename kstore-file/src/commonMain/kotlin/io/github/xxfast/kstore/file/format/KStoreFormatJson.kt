package io.github.xxfast.kstore.file.format

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

public class KStoreFormatJson<T : @Serializable Any>(private val json: Json, serializer: KSerializer<T>) :
  KStoreFormat<T>(serializer) {
  @OptIn(ExperimentalSerializationApi::class)
  override fun <T> decodeFromSource(deserializer: DeserializationStrategy<T>, source: Source): T {
    return json.decodeFromSource(deserializer, source)
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun <T> encodeToSink(serializer: SerializationStrategy<T>, value: T, sink: Sink) {
    json.encodeToSink(serializer, value, sink)
  }
}