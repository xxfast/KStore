package io.github.xxfast.kstore.file


import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.format.KStoreFormat
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public inline fun <reified T : @Serializable Any> FileCodec(
  file: Path,
  format: KStoreFormat<T>,
  ): FileCodec<T> = FileCodec(
  file = file,
  format = format,
  serializer = format.serializer,
)

public class FileCodec<T : @Serializable Any>(
  private val file: Path,
  private val format: KStoreFormat<T>,
  private val serializer: KSerializer<T>,
) : Codec<T> {
  override suspend fun decode(): T? =
    try {
      format.decodeFromSource(serializer, SystemFileSystem.source(file).buffered())
    } catch (e: FileNotFoundException) {
      null
    }

  override suspend fun encode(value: T?) {
    if (value != null) format.encodeToSink(serializer, value, SystemFileSystem.sink(file).buffered())
    else SystemFileSystem.delete(file)
  }
}
