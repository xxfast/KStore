package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storeOf
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlinx.serialization.json.io.decodeFromSource as decode
import kotlinx.serialization.json.io.encodeToSink as encode

/**
 * Creates a store with a versioned encoder and decoder
 * Note: An additional file will be written to manage metadata on the same path with `.version` suffix
 *
 * @param file path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param json Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
 * @param versionPath path to the file that contains the current version of the store
 * @param migration Migration strategy to use. Defaults
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  file: Path,
  version: Int,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = DefaultJson,
  versionPath: Path = Path("$file.version"), // TODO: Save to file metadata instead
  noinline migration: Migration<T> = DefaultMigration(default),
): KStore<T> = storeOf(
  codec = VersionedCodec(file, version, json, json.serializersModule.serializer(), migration, versionPath),
  default = default,
  enableCache = enableCache,
)

@Suppress("FunctionName") // Fake constructor
public fun <T> DefaultMigration(default: T?): Migration<T> = { _, _ -> default }

public typealias Migration<T> = (version: Int?, JsonElement?) -> T?

@OptIn(ExperimentalSerializationApi::class)
public class VersionedCodec<T : @Serializable Any>(
  private val file: Path,
  private val version: Int = 0,
  private val json: Json,
  private val serializer: KSerializer<T>,
  private val migration: Migration<T>,
  private val versionPath: Path = Path("$file.version"), // TODO: Save to file metadata instead
) : Codec<T> {

  override suspend fun decode(): T? =
    try {
      json.decode(serializer, SystemFileSystem.source(file).buffered())
    } catch (e: SerializationException) {
      val previousVersion: Int =
        if (SystemFileSystem.exists(versionPath)) json.decode(
          Int.serializer(),
          SystemFileSystem.source(versionPath).buffered()
        )
        else 0

      val data: JsonElement = json.decode(SystemFileSystem.source(file).buffered())
      migration(previousVersion, data)
    } catch (e: FileNotFoundException) {
      null
    }

  override suspend fun encode(value: T?) {
    if (value != null) {
      SystemFileSystem.sink(versionPath).buffered().use { json.encode(Int.serializer(), version, it) }
      SystemFileSystem.sink(file).buffered().use { json.encode(serializer, value, it) }
    } else {
      SystemFileSystem.delete(versionPath, mustExist = false)
      SystemFileSystem.delete(file, mustExist = false)
    }
  }
}
