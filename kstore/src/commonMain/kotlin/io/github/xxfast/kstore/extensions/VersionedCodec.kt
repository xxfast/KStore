package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Recovers a value from a previous shape of the store.
 *
 * @param version version that the store was last written with, or null when it cannot be determined
 * (e.g. the version record itself is corrupt)
 * @param previous the previously stored payload in a raw shape [R] (e.g. a JsonElement), or null when
 * it cannot be read at all
 * @return the recovered value, or null when there is nothing to recover
 */
public typealias Migration<T, R> = (version: Int?, previous: R?) -> T?

/** A [Migration] that discards the previous value and recovers with [default] */
@Suppress("FunctionName") // Fake constructor
public fun <T, R> DefaultMigration(default: T?): Migration<T, R> = { _, _ -> default }

/**
 * Decorates a [data] codec with versioning. When [data] fails to decode because the persisted value
 * no longer matches the current shape of [T], the previously stored version and payload are handed
 * to [migration] to recover a value.
 *
 * Everything version-specific is itself just persistence, so it is delegated to codecs:
 * [versionCodec] persists the current [version] alongside the data, and [staleCodec] re-reads the
 * old payload in a raw shape [R] that survives schema changes. For any backend, both are typically
 * the backend's own codec pointed at a sibling record; see the `storeOf(file, version, ...)` and
 * `storeOf(key, version, ...)` factories in the backend modules.
 *
 * @param data codec for the current shape of [T]
 * @param version current version of the store
 * @param versionCodec codec that persists the version. Reading null is taken to mean the store
 * predates versioning, and reads as version 0
 * @param staleCodec codec that re-reads the old payload in a raw shape [R] for [migration]
 * @param migration migration strategy to recover a value from an older (or corrupt) store
 */
public class VersionedCodec<T : @Serializable Any, R : Any>(
  private val data: Codec<T>,
  private val version: Int,
  private val versionCodec: Codec<Int>,
  private val staleCodec: Codec<R>,
  private val migration: Migration<T, R>,
) : Codec<T> {

  /**
   * Decodes the stored value.
   * If [data] cannot decode it, the value is recovered through [migration]. A missing version
   * record reads as version 0 (the store predates versioning); one that exists but cannot be read
   * leaves the version unknown, same as an unreadable payload.
   * @return optional value that is decoded
   */
  override suspend fun decode(): T? =
    try {
      data.decode()
    } catch (e: SerializationException) {
      migration(
        try { versionCodec.decode() ?: 0 } catch (e: SerializationException) { null },
        try { staleCodec.decode() } catch (e: SerializationException) { null },
      )
    }

  /**
   * Encodes the given value along with the current [version].
   * Data first, version second. A crash in between leaves the new data with a stale version, which
   * still decodes directly. The reverse order would claim a version the data hasn't been written to.
   * @param value optional value to encode
   */
  override suspend fun encode(value: T?) {
    data.encode(value)
    versionCodec.encode(if (value != null) version else null)
  }
}

/**
 * Decorates this codec with versioning - see [VersionedCodec]
 *
 * @param version current version of the store
 * @param versionCodec codec that persists the version
 * @param staleCodec codec that re-reads the old payload in a raw shape [R] for [migration]
 * @param migration migration strategy to recover a value from an older (or corrupt) store
 * @return codec that migrates the stored value to the current shape of [T]
 */
public fun <T : @Serializable Any, R : Any> Codec<T>.versioned(
  version: Int,
  versionCodec: Codec<Int>,
  staleCodec: Codec<R>,
  migration: Migration<T, R>,
): Codec<T> = VersionedCodec(
  data = this,
  version = version,
  versionCodec = versionCodec,
  staleCodec = staleCodec,
  migration = migration,
)
