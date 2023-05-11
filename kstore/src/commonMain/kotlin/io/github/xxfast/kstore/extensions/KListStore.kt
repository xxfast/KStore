package io.github.xxfast.kstore.extensions

import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable

/**
 * Get a list of type [T] from the store, or empty list if the store is empty
 *
 * @return stored list of type [T]
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.getOrEmpty(): List<T> =
  get() ?: emptyList()

/**
 * Get an item from list of type [T] from the store, or null if the store is empty
 *
 * @param index index of the item from the list
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.get(index: Int): T? =
  get()?.get(index)

/**
 * Add item(s) to the end of the list of type [T] to the store
 *
 * @param value item(s) to be added to the end of the list
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.plus(vararg value: T) {
  update { list -> list?.plus(value) ?: listOf(*value) }
}

/**
 * Remove item(s) of type [T] from the list in the store
 *
 * @param value item(s) to be removed from the list
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.minus(vararg value: T) {
  update { list -> list?.minus(value.toSet()) ?: emptyList() }
}

/**
 * Updates the list by applying the given [operation] lambda to each element in the stored list.
 *
 * @param operation lambda to update each list item
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.map(operation: (T) -> T) {
  update { list -> list?.map { t -> operation(t) } }
}

/**
 * Updates the list by applying the given [operation] lambda to each element in the stored list and
 * its index in the stored collection.
 *
 * @param operation lambda to update each list item
 */
public suspend fun <T : @Serializable Any> KStore<List<T>>.mapIndexed(operation: (Int, T) -> T) {
  update { list -> list?.mapIndexed { index, t -> operation(index, t) } }
}

/** Observe a list store for updates */
public val <T : @Serializable Any> KStore<List<T>>.updatesOrEmpty: Flow<List<T>>
  get() =
  updates.filterNotNull()
