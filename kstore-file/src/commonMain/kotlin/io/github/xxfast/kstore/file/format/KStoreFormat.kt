package io.github.xxfast.kstore.file.format

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

public abstract class KStoreFormat<T : @Serializable Any>(public val serializer: KSerializer<T>) {

  public abstract fun <T> decodeFromSource(
    deserializer: DeserializationStrategy<T>,
    source: Source
  ): T

  public abstract fun <T> encodeToSink(
    serializer: SerializationStrategy<T>,
    value: T,
    sink: Sink
  )

}