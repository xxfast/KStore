package io.github.xxfast.kstore.file.utils

import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@ExperimentalKStoreApi
@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
public val NSFileManager.DocumentDirectory: NSURL? get() = URLForDirectory(
  directory = NSDocumentDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)

@ExperimentalKStoreApi
@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
public val NSFileManager.CachesDirectory: NSURL? get() = URLForDirectory(
  directory = NSCachesDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)