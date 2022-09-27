package io.github.xxfast.kstore.utils

import okio.FileSystem
import okio.NodeJsFileSystem

actual val FILE_SYSTEM: FileSystem = NodeJsFileSystem
