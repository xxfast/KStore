package io.github.xxfast.kstore.file.utils

import okio.FileSystem
import okio.NodeJsFileSystem

public actual val FILE_SYSTEM: FileSystem = NodeJsFileSystem
