package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.utils.FILE_SYSTEM
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import okio.FileNotFoundException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

/**
 * Creates a store with a versioned encoder and decoder
 * Note: An additional file will be written to manage metadata on the same path with `.version` suffix
 *
 * @param filePath path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param json Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
 * @param migration Migration strategy to use. Defaults
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  file: Path,
  version: Int,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
  noinline migration: Migration<T> = DefaultMigration(default),
): KStore<T> {
  val codec: Codec<T> = VersionedCodec(file, version, json, json.serializersModule.serializer(), migration)
  return KStore(default, enableCache, codec)
}

@Suppress("FunctionName") // Fake constructor
public fun <T> DefaultMigration(default: T?): Migration<T> = { _, _ -> default }

public typealias Migration<T> = (version: Int?, JsonElement?) -> T?

@OptIn(ExperimentalSerializationApi::class)
public class VersionedCodec<T: @Serializable Any>(
  private val file: Path,
  private val version: Int = 0,
  private val json: Json,
  private val serializer: KSerializer<T>,
  private val migration: Migration<T>,
): Codec<T> {
  private val versionPath: Path = "$${file.name}.version".toPath() // TODO: Save to file metadata instead

  override suspend fun decode(): T? =
    try {
      json.decode(serializer, FILE_SYSTEM.source(file).buffer())
    } catch (e: SerializationException) {
      val previousVersion: Int =
        if (FILE_SYSTEM.exists(versionPath)) json.decode(Int.serializer(), FILE_SYSTEM.source(versionPath).buffer())
        else 0

      val data: JsonElement = json.decode(FILE_SYSTEM.source(file).buffer())
      migration(previousVersion, data)
    } catch (e: FileNotFoundException) {
      null
    }

  override suspend fun encode(value: T?) {
    if (value != null) {
      FILE_SYSTEM.sink(versionPath).buffer().use { json.encode(Int.serializer(), version, it) }
      FILE_SYSTEM.sink(file).buffer().use { json.encode(serializer, value, it) }
    } else {
      FILE_SYSTEM.delete(versionPath)
      FILE_SYSTEM.delete(file)
    }
  }
}
