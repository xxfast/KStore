package io.github.xxfast.kstore.file

import android.content.Context
import androidx.startup.Initializer
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi

@ExperimentalKStoreApi
public class DirectoriesInitializer: Initializer<DirectoryProvider> {
  override fun create(context: Context): DirectoryProvider = AndroidDirectoryProvider(context)
    .also { _directories = it }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}