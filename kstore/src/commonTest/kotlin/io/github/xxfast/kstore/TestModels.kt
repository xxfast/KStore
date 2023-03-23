package io.github.xxfast.kstore

import kotlinx.serialization.Serializable

@Serializable
sealed class Pet {
  abstract val name: String
  abstract val age: Int
}

@Serializable
data class Cat(
  override val name: String,
  override val age: Int,
  val lives: Int = 9,
) : Pet()

internal val MYLO = Cat(name = "Mylo", age = 1)
internal val OREO = Cat(name = "Oreo", age = 1)

@Serializable
data class Kennel<T : Pet>(val pet: T)
