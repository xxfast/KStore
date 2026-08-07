package io.github.xxfast.kstore.file


import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlin.uuid.Uuid
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

/**
 * Creates a store with [FileCodec] with json serializer
 * @param file path to the file that is managed by this store
 * @param tempFile staging file this codec writes through. Defaults to a path unique to this codec,
 * so that codecs sharing a [file] never stage into the same buffer - see [uniqueTempFile]
 * @param json JSON Serializer to use. defaults to [DefaultJson]
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> FileCodec(
  file: Path,
  tempFile: Path = uniqueTempFile(file),
  json: Json = DefaultJson,
): FileCodec<T> = FileCodec(
  file = file,
  tempFile = tempFile,
  json = json,
  serializer = json.serializersModule.serializer(),
)

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
  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun decode(): T? =
    try {
      SystemFileSystem.source(file).buffered().use { json.decode(serializer, it) }
    } catch (e: FileNotFoundException) {
      null
    }

  /**
   * Encodes the given value to the file.
   * If the value is null, the file is deleted.
   * If the encoding fails, the temp file is deleted.
   * If the encoding succeeds, the temp file is atomically moved to the target file - completing the transaction.
   * On platforms where atomic move is not supported (e.g., Android 7 and below) this falls back to a
   * non-atomic copy-and-delete; the transactional guarantee does not hold for that fallback path.
   * @param value optional value to encode
   */
  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun encode(value: T?) {
    if (value == null) {
      SystemFileSystem.delete(file, mustExist = false)
      return
    }

    try {
      SystemFileSystem.sink(tempFile).buffered().use { json.encode(serializer, value, it) }
    } catch (e: Throwable) {
      SystemFileSystem.delete(tempFile, mustExist = false)
      throw e
    }

    moveOrCopy(source = tempFile, destination = file)
  }
}

/**
 * A staging path unique to a single codec, of the form `<file>.<random>.temp`.
 *
 * Stores pointing at the same [file] each get their own staging file. Sharing one lets concurrent
 * writes interleave into a single buffer before either is moved into place, and whichever moves
 * last publishes the mixture, which is how a store ends up corrupt on disk (see issue #85). With a
 * staging file each, the move stays atomic and the worst case is last write wins.
 *
 * Note this is one staging file per codec, not per write, so a process killed mid-write leaves at
 * most one behind per store. The next write is unaffected either way.
 *
 * The suffix comes from [Uuid.random] rather than [kotlin.random.Random] on purpose. Random.Default
 * is a PRNG seeded from the clock on some targets, so two processes cold starting in the same tick
 * can draw the same sequence and land on the same staging path, which is the one case this is meant
 * to prevent. Uuid.random draws from the platform's secure source and has no shared seed.
 *
 * @param file path to the file being staged for
 * @return a staging path that no other codec will pick
 */
public fun uniqueTempFile(file: Path): Path {
  val suffix: String = Uuid.random().toHexString()
  return Path("$file.$suffix.temp")
}

/**
 * Moves [source] onto [destination] atomically via [atomicMove].
 *
 * On platforms where atomic move is unsupported (e.g., Android 7 and below - see issue #137) this falls
 * back to a non-atomic copy-and-delete. The fallback is not transactional: a crash mid-copy can leave the
 * destination partially written. On a failed copy the (possibly corrupt) destination is removed.
 *
 * The [atomicMove] parameter is a seam over SystemFileSystem.atomicMove so the fallback path can be exercised in tests.
 */
internal fun moveOrCopy(
  source: Path,
  destination: Path,
  atomicMove: (source: Path, destination: Path) -> Unit = SystemFileSystem::atomicMove,
) {
  try {
    atomicMove(source, destination)
  } catch (_: UnsupportedOperationException) {
    try {
      SystemFileSystem.source(source).buffered().use { from ->
        SystemFileSystem.sink(destination).buffered().use { to ->
          from.transferTo(to)
        }
      }
    } catch (e: Throwable) {
      SystemFileSystem.delete(destination, mustExist = false)
      throw e
    } finally {
      SystemFileSystem.delete(source, mustExist = false)
    }
  }
}
