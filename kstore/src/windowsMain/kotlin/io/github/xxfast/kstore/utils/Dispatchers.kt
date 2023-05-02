package io.github.xxfast.kstore.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val StoreDispatcher: CoroutineDispatcher = Dispatchers.IO
