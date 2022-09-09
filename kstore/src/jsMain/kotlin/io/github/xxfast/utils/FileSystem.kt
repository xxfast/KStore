package io.github.xxfast.utils

import okio.FileSystem
import okio.NodeJsFileSystem

actual val FILE_SYSTEM: FileSystem = NodeJsFileSystem
