package io.github.xxfast.kstore.storage.extensions

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.DefaultMigration
import io.github.xxfast.kstore.extensions.Migration
import io.github.xxfast.kstore.extensions.versioned
import io.github.xxfast.kstore.storage.Storage
import io.github.xxfast.kstore.storage.StorageCodec
import io.github.xxfast.kstore.storage.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Creates a versioned store with [StorageCodec]
 * Note: An additional record will be written to manage metadata on the same key with `@version` suffix
 *
 * @param key key for the record that is managed by this store
 * @param version current version of the store
 * @param default returns this value if the record is not found. defaults to null
 * @param enableCache maintain a cache. If set to false, it always reads from storage
 * @param json Serializer to use. defaults to [DefaultJson]
 * @param storage storage to use. defaults to [localStorage]
 * @param versionKey key for the record that contains the current version of the store
 * @param migration Migration strategy to use. Defaults to [DefaultMigration]
 *
 * @return store that contains a value of type [T]
 */
public inline fun <reified T : @Serializable Any> storeOf(
  key: String,
  version: Int,
  default: T? = null,
  enableCache: Boolean = true,
  json: Json = DefaultJson,
  storage: Storage = localStorage,
  versionKey: String = "$key@version",
  noinline migration: Migration<T, JsonElement> = DefaultMigration(default),
): KStore<T> = KStore(
  default = default,
  enableCache = enableCache,
  codec = StorageCodec<T>(key = key, format = json, storage = storage).versioned(
    version = version,
    versionCodec = StorageCodec(key = versionKey, format = json, storage = storage),
    staleCodec = StorageCodec<JsonElement>(key = key, format = json, storage = storage),
    migration = migration,
  ),
)
