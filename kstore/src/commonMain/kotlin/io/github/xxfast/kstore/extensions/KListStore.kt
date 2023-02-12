package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Creates a store that contains a list
 *
 * @param filePath path to the file that is managed by this store
 * @param default returns this value if the file is not found. defaults to empty list
 * @param enableCache maintain a cache. If set to false, it always reads from disk
 * @param serializer Serializer to use. Defaults serializer ignores unknown keys and encodes the defaults
 *
 * @return store that contains a list of type [T]
 */
inline fun <reified T : @Serializable Any> listStoreOf(
  filePath: String,
  default: List<T> = emptyList(),
  enableCache: Boolean = true,
  serializer: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
): KStore<List<T>> =
  storeOf(filePath, default, enableCache, serializer)

/**
 * Get a list of type [T] from the store, or empty list if the store is empty
 *
 * @return stored list of type [T]
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.getOrEmpty(): List<T> =
  get() ?: emptyList()

/**
 * Get an item from list of type [T] from the store, or null if the store is empty
 *
 * @param index index of the item from the list
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.get(index: Int): T? =
  get()?.get(index)

/**
 * Add item(s) to the end of the list of type [T] to the store
 *
 * @param value item(s) to be added to the end of the list
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.plus(vararg value: T) {
  update { list -> list?.plus(value) ?: listOf(*value) }
}

/**
 * Remove item(s) of type [T] from the list in the store
 *
 * @param value item(s) to be removed from the list
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.minus(vararg value: T) =
  update { list -> list?.minus(value.toSet()) ?: emptyList() }

/**
 * Updates the list by applying the given [operation] lambda to each element in the stored list.
 *
 * @param operation lambda to update each list item
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.map(operation: (T) -> T) {
  update { list -> list?.map { t -> operation(t) } }
}

/**
 * Updates the list by applying the given [operation] lambda to each element in the stored list and
 * its index in the stored collection.
 *
 * @param operation lambda to update each list item
 */
suspend fun <T : @Serializable Any> KStore<List<T>>.mapIndexed(operation: (Int, T) -> T) {
  update { list -> list?.mapIndexed { index, t -> operation(index, t) } }
}

/** Observe a list store for updates */
val <T : @Serializable Any> KStore<List<T>>.updatesOrEmpty: Flow<List<T>> get() =
  updates.filterNotNull()
