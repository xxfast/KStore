package io.github.xxfast

import io.github.xxfast.utils.FILE_SYSTEM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.*

@OptIn(ExperimentalSerializationApi::class)
class KStore<T : Any>(
    val path: Path,
    default: T? = null,
    val serializer: Json = Json,
    val lock: Mutex = Mutex(),
) {
  val stateFlow: MutableStateFlow<T?> = MutableStateFlow(default)

  val updates: Flow<T?> get() = this.stateFlow

  suspend inline fun <reified V : T> get(): T? = lock.withLock {
    val cached: T? = stateFlow.value
    if (cached != null) return@withLock cached

    val decoded: V? = try {
      val source: BufferedSource = FILE_SYSTEM.source(path).buffer()
      serializer.decodeFromBufferedSource(source)
    } catch (e: Exception) {
      null
    }

    if (stateFlow.value == null && decoded != null) stateFlow.emit(decoded)
    return@withLock decoded
  }

  suspend inline fun <reified V : T> set(value: V?) = lock.withLock {
    stateFlow.emit(value)
    val sink: BufferedSink = FILE_SYSTEM.sink(path).buffer()
    sink.use { serializer.encodeToBufferedSink(value, it) }
  }

  suspend fun clear(){
    FILE_SYSTEM.delete(path)
    stateFlow.emit(null)
  }
}

