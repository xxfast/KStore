package io.github.xxfast.kstore

import io.github.xxfast.kstore.utils.FILE_SYSTEM
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okio.FileMetadata
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

/**
 * TODO: Remove the wrapper once we can write [FileMetadata] with okio-multiplatform
  */
@Serializable
data class Versioned<T : @Serializable Any>(val version: Int, val value: T?)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : @Serializable Any> storeOf(
  filePath: String,
  version: Int,
  default: T? = null,
  enableCache: Boolean = true,
  serializer: Json = Json { ignoreUnknownKeys = true },
  crossinline migration: (version: Int, JsonElement?) -> T?  = { _, _ -> default },
): KStore<T> {
  val encoder: (T?) -> Unit = { value: T? ->
    val versioned: Versioned<T> = Versioned(version, value)
    FILE_SYSTEM.sink(filePath.toPath()).buffer().use { serializer.encode(versioned, it) }
  }

  val decoder: () -> T? = {
    try {
      val versioned: Versioned<T> = serializer.decode(FILE_SYSTEM.source(filePath.toPath()).buffer())
      versioned.value
    } catch (e: SerializationException){
      val previous: Versioned<JsonElement> = serializer.decode(FILE_SYSTEM.source(filePath.toPath()).buffer())
      migration(previous.version, previous.value)
    }
  }

  return KStore(filePath.toPath(), default, enableCache, encoder, decoder)
}