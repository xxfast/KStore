@file:OptIn(ExperimentalSerializationApi::class)

package io.github.xxfast.kstore.file.extensions

import io.github.xxfast.kstore.file.utils.FILE_SYSTEM
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

/**
 * Creates a store that manages a folder that contains values of type [T]
 *
 * @param folderPath path to the folder that is managed by this store
 * @param serializer Serializer to use.
 * @param indexWith lambda that returns a index of the file given the the value [T]
 *
 * @return store that contains a folder of values type [T]
 */
@ExperimentalKStoreApi
public inline fun <reified T : @Serializable Any> folderOf(
  folderPath: String,
  serializer: Json = Json,
  noinline indexWith: (T) -> String,
): KFolder<T> {
  val encoder: (BufferedSink, T?) -> Unit = { sink: BufferedSink, value: T? -> sink.use { serializer.encode(value, it) } }
  val decoder: (BufferedSource) -> T? = { source: BufferedSource -> serializer.decode(source) }
  return KFolder(folderPath, indexWith, encoder, decoder)
}

/**
 * Creates a store that manages a folder that contains values of type [T] with a custom encoder and decoder
 *
 * @param folderPath path to the folder that is managed by this store
 * @param indexWith lambda that returns a index of the file given the the value [T]
 * @param encoder lambda to encode the values with
 * @param decoder lambda to decode the values with
 *
 * @return store that contains a folder of values type [T]
 */
@ExperimentalKStoreApi
public class KFolder<T : @Serializable Any>(
  private val folderPath: String,
  private val indexWith: (T) -> String,
  private val encoder: (BufferedSink, T?) -> Unit,
  private val decoder: (BufferedSource) -> T?
) {
  private val lock: Mutex = Mutex()
  private val stateFlow: MutableStateFlow<Set<String>> = MutableStateFlow(indexes)

  public val indexes: Set<String> get() {
    if(!FILE_SYSTEM.exists(folderPath.toPath())) return emptySet()
    return FILE_SYSTEM.list(folderPath.toPath()).map { it.name }.toSet()
  }

  public val indexUpdates: Flow<Set<String>> = stateFlow

  public suspend fun get(index: String): T? = lock.withLock {
    val path: Path = "$folderPath/$index".toPath()
    if(!FILE_SYSTEM.exists(path)) return@withLock null
    decoder(FILE_SYSTEM.source(path).buffer())
  }

  public suspend fun add(value: T) {
    if(!FILE_SYSTEM.exists(folderPath.toPath()))
      FILE_SYSTEM.createDirectory(folderPath.toPath(), false)

    val index: String = indexWith(value)
    lock.withLock { encoder(FILE_SYSTEM.sink("$folderPath/$index".toPath()).buffer(), value) }
    stateFlow.emit(indexes)
  }

  public suspend fun remove(index: String) {
    if(!FILE_SYSTEM.exists(folderPath.toPath())) return
    lock.withLock { FILE_SYSTEM.delete("$folderPath/$index".toPath()) }
    stateFlow.emit(indexes)
  }

  public suspend fun delete(){
    if(!FILE_SYSTEM.exists(folderPath.toPath())) return
    lock.withLock { FILE_SYSTEM.deleteRecursively(folderPath.toPath()) }
    stateFlow.emit(indexes)
  }
}
