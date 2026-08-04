package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.moveOrCopy
import io.github.xxfast.kstore.file.uniqueTempFile
import io.github.xxfast.kstore.storeOf
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.DeserializationStrategy
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
  private val tempPath: Path = uniqueTempFile(file),
  private val tempVersionPath: Path = uniqueTempFile(versionPath),
) : Codec<T> {

  /**
   * Decodes the file to a value.
   * If the file does not exist, null is returned.
   * If the file does not hold the current shape of [T], the value is recovered through [migration].
   * @return optional value that is decoded
   */
  override suspend fun decode(): T? =
    try {
      SystemFileSystem.source(file).buffered().use { json.decode(serializer, it) }
    } catch (e: FileNotFoundException) {
      null
    } catch (e: SerializationException) {
      // The file doesn't hold the current shape of [T]. Either it was written by an older version of
      // this store - which [migration] can recover from - or it is corrupt/partially written, in which
      // case there is nothing to recover and [migration] is handed what little is known.
      // No version file at all means the store predates versioning, so it reads as 0. One that exists
      // but can't be read leaves the version unknown, same as unreadable data.
      migration(
        decodeOrNull(versionPath, Int.serializer(), whenMissing = 0),
        decodeOrNull(file, JsonElement.serializer(), whenMissing = null),
      )
    }

  /**
   * Reads [path] with [deserializer], degrading rather than throwing so that a corrupt store can be
   * migrated or reset instead of crashing on every read.
   * @return [whenMissing] when there is no such file, or null when its contents cannot be decoded
   */
  private fun <R : Any> decodeOrNull(
    path: Path,
    deserializer: DeserializationStrategy<R>,
    whenMissing: R?,
  ): R? =
    try {
      SystemFileSystem.source(path).buffered().use { json.decode(deserializer, it) }
    } catch (e: FileNotFoundException) {
      whenMissing
    } catch (e: SerializationException) {
      null
    }

  /**
   * Encodes the given value to the file, along with the current [version].
   * If the value is null, both files are deleted.
   * If the encoding fails, the temp files are deleted.
   * On platforms where atomic move is not supported (e.g., Android 7 and below) this falls back to a
   * non-atomic copy-and-delete; the transactional guarantee does not hold for that fallback path.
   * @param value optional value to encode
   */
  override suspend fun encode(value: T?) {
    if (value == null) {
      SystemFileSystem.delete(versionPath, mustExist = false)
      SystemFileSystem.delete(file, mustExist = false)
      return
    }

    try {
      SystemFileSystem.sink(tempPath).buffered().use { json.encode(serializer, value, it) }
      SystemFileSystem.sink(tempVersionPath).buffered().use { json.encode(Int.serializer(), version, it) }
    } catch (e: Throwable) {
      SystemFileSystem.delete(tempPath, mustExist = false)
      SystemFileSystem.delete(tempVersionPath, mustExist = false)
      throw e
    }

    // Data first, version second. A crash in between leaves the new data with a stale version, which
    // still decodes directly. The reverse order would claim a version the data hasn't been written to.
    moveOrCopy(source = tempPath, destination = file)
    moveOrCopy(source = tempVersionPath, destination = versionPath)
  }
}
