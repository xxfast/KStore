package io.github.xxfast.kstore.storage

import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

public actual typealias Storage = Storage

internal actual operator fun Storage.get(key: String): String? = this[key]

internal actual operator fun Storage.set(key: String, value: String){
  this[key] = value
}

internal actual fun Storage.remove(key: String) = this.removeItem(key)

internal actual fun Storage.delete() = this.clear()

public actual val localStorage: Storage = kotlinx.browser.localStorage
