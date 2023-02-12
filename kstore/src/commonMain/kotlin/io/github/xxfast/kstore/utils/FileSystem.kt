package io.github.xxfast.kstore.utils

import okio.FileSystem

/***
 * Okio file system for the given platform
 */
expect val FILE_SYSTEM: FileSystem
