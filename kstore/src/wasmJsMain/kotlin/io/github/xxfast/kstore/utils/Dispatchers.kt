package io.github.xxfast.kstore.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val StoreDispatcher: CoroutineDispatcher = Dispatchers.Default
