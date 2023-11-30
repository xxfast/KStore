package io.github.xxfast.kstore.file


import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.file.utils.FILE_SYSTEM
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.FileNotFoundException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

public inline fun <reified T : @Serializable Any> FileCodec(
  file: Path,
  json: Json = DefaultJson,
): FileCodec<T> = FileCodec(
  file = file,
  json = json,
  serializer = json.serializersModule.serializer(),
)

@OptIn(ExperimentalSerializationApi::class)
public class FileCodec<T : @Serializable Any>(
  private val file: Path,
  private val json: Json,
  private val serializer: KSerializer<T>,
) : Codec<T> {
  override suspend fun decode(): T? =
    try { json.decode(serializer, FILE_SYSTEM.source(file).buffer()) }
    catch (e: FileNotFoundException) { null }

  override suspend fun encode(value: T?) {
    if (value != null) FILE_SYSTEM.sink(file).buffer().use { json.encode(serializer, value, it) }
    else FILE_SYSTEM.delete(file)
  }
}
