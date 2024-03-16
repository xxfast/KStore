package io.github.xxfast.kstore.storage

public expect abstract class Storage

internal expect operator fun Storage.get(key: String): String?

internal expect operator fun Storage.set(key: String, value: String)

internal expect fun Storage.remove(key: String)

internal expect fun Storage.delete()

public expect val localStorage: Storage
