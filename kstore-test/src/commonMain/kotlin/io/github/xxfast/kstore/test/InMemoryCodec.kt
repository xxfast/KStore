package io.github.xxfast.kstore.test

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer


/**
 * A codec that stores data in memory.
 */
public inline fun <reified T : @Serializable Any> InMemoryCodec(
  format: StringFormat = DefaultJson,
): InMemoryCodec<T> = InMemoryCodec(format, format.serializersModule.serializer())

/**
 * A codec that stores data in memory.
 */
public class InMemoryCodec<T : @Serializable Any>(
  private val format: StringFormat,
  private val serializer: KSerializer<T>
) : Codec<T> {

  private var storedData: String? = null

  override suspend fun encode(value: T?) {
    storedData = value?.let { format.encodeToString(serializer, it) }
  }

  override suspend fun decode(): T? = storedData?.let {
    format.decodeFromString(serializer, it)
  }

}
