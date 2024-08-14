package io.github.xxfast.kstore.file


import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlinx.serialization.serializer

/**
 * Creates a store with [FileCodec] with json serializer
 * @param file path to the file that is managed by this store
 * @param json JSON Serializer to use. defaults to [DefaultJson]
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> FileCodec(
  file: Path,
  json: Json = DefaultJson,
): FileCodec<T> = FileCodec(
  file = file,
  json = json,
  serializer = json.serializersModule.serializer(),
)

public class FileCodec<T : @Serializable Any>(
  private val file: Path,
  private val json: Json,
  private val serializer: KSerializer<T>,
) : Codec<T> {

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun decode(): T? =
    try {
      SystemFileSystem.source(file).buffered().use { json.decodeFromSource(serializer, it) }
    } catch (e: FileNotFoundException) {
      null
    }

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun encode(value: T?) {
    if (value != null) SystemFileSystem.sink(file).buffered().use { json.encodeToSink(serializer, value, it) }
    else SystemFileSystem.delete(file)
  }
}
