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
import kotlinx.serialization.serializer
import kotlinx.serialization.json.io.decodeFromSource as decode
import kotlinx.serialization.json.io.encodeToSink as encode

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
      json.decode(serializer, SystemFileSystem.source(file).buffered())
    } catch (e: FileNotFoundException) {
      null
    }

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun encode(value: T?) {
    if (value != null) json.encode(serializer, value, SystemFileSystem.sink(file).buffered())
    else SystemFileSystem.delete(file)
  }
}
