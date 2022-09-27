package io.github.xxfast.kstore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

inline fun <reified T : @Serializable Any> listStoreOf(
  filePath: String,
  default: List<T> = emptyList(),
  enableCache: Boolean = true,
  serializer: Json = Json,
): KStore<List<T>> =
  storeOf(filePath, default, enableCache, serializer)

suspend fun <T : @Serializable Any> KStore<List<T>>.getOrEmpty(): List<T> =
  get() ?: emptyList()

suspend fun <T : @Serializable Any> KStore<List<T>>.get(index: Int): T? =
  get()?.get(index)

suspend fun <T : @Serializable Any> KStore<List<T>>.plus(vararg value: T) {
  update { list -> list?.plus(value) ?: listOf(*value) }
}

suspend fun <T : @Serializable Any> KStore<List<T>>.minus(vararg value: T) =
  update { list -> list?.minus(value.toSet()) ?: emptyList()  }

suspend fun <T : @Serializable Any> KStore<List<T>>.map(operation: (T) -> T) {
  update { list -> list?.map { t -> operation(t) } }
}

suspend fun <T : @Serializable Any> KStore<List<T>>.mapIndexed(operation: (Int, T) -> T) {
  update { list -> list?.mapIndexed { index, t -> operation(index, t) } }
}

val <T : @Serializable Any> KStore<List<T>>.updatesOrEmpty: Flow<List<T>> get() =
  updates.filterNotNull()
