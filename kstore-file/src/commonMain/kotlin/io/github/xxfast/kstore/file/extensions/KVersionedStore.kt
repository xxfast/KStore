package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.versioned
import io.github.xxfast.kstore.file.FileCodec
import io.github.xxfast.kstore.file.uniqueTempFile
import io.github.xxfast.kstore.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

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

/** A [io.github.xxfast.kstore.extensions.Migration] where the raw shape is a [JsonElement] */
public typealias Migration<T> = io.github.xxfast.kstore.extensions.Migration<T, JsonElement>

/**
 * Creates a file codec with versioning - a [FileCodec] decorated with
 * [io.github.xxfast.kstore.extensions.VersionedCodec]. The version is persisted to [versionPath]
 * through its own [FileCodec], and old payloads are re-read as [JsonElement] for [migration].
 *
 * @param file path to the file that is managed by this codec
 * @param version current version of the store
 * @param json Serializer to use
 * @param serializer serializer for the current shape of [T]
 * @param migration Migration strategy to use
 * @param versionPath path to the file that contains the current version of the store
 * @param tempPath staging file the data codec writes through - see [uniqueTempFile]
 * @param tempVersionPath staging file the version codec writes through - see [uniqueTempFile]
 */
@Suppress("FunctionName") // Fake constructor
public fun <T : @Serializable Any> VersionedCodec(
  file: Path,
  version: Int = 0,
  json: Json,
  serializer: KSerializer<T>,
  migration: Migration<T>,
  versionPath: Path = Path("$file.version"), // TODO: Save to file metadata instead
  tempPath: Path = uniqueTempFile(file),
  tempVersionPath: Path = uniqueTempFile(versionPath),
): Codec<T> = FileCodec(
  file = file,
  tempFile = tempPath,
  json = json,
  serializer = serializer,
).versioned(
  version = version,
  versionCodec = FileCodec(
    file = versionPath,
    tempFile = tempVersionPath,
    json = json,
    serializer = Int.serializer(),
  ),
  staleCodec = FileCodec(
    file = file,
    tempFile = uniqueTempFile(file), // never written through - the stale codec only reads
    json = json,
    serializer = JsonElement.serializer(),
  ),
  migration = migration,
)
