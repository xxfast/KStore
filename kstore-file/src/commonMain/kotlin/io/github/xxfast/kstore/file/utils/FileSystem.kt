package io.github.xxfast.kstore.file.utils

import okio.FileSystem

/***
 * Okio file system for the given platform
 */
public expect val FILE_SYSTEM: FileSystem
