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
  tempFile: Path = "${file.name}.temp".toPath(),
  json: Json = DefaultJson,
): FileCodec<T> = FileCodec(
  file = file,
  tempFile = tempFile,
  json = json,
  serializer = json.serializersModule.serializer(),
)

@OptIn(ExperimentalSerializationApi::class)
public class FileCodec<T : @Serializable Any>(
  private val file: Path,
  private val tempFile: Path,
  private val json: Json,
  private val serializer: KSerializer<T>,
) : Codec<T> {
  /**
   * Decodes the file to a value.
   * If the file does not exist, null is returned.
   * @return optional value that is decoded
   */
  override suspend fun decode(): T? =
    try { json.decode(serializer, FILE_SYSTEM.source(file).buffer()) }
    catch (e: FileNotFoundException) { null }

  /**
   * Encodes the given value to the file.
   * If the value is null, the file is deleted.
   * If the encoding fails, the temp file is deleted.
   * If the encoding succeeds, the temp file is atomically moved to the target file - completing the transaction.
   * @param value optional value to encode
   */
  override suspend fun encode(value: T?) {
    try {
      if (value != null) FILE_SYSTEM.sink(tempFile).buffer().use { json.encode(serializer, value, it) }
      else FILE_SYSTEM.delete(tempFile)
    } catch (e: Throwable) {
      FILE_SYSTEM.delete(tempFile)
      throw e
    }

    FILE_SYSTEM.atomicMove(source = tempFile, target = file)
  }
}
