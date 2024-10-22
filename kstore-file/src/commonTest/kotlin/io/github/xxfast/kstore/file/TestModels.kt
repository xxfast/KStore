package io.github.xxfast.kstore.file

import io.github.xxfast.kstore.file.RobotCat.Id
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed interface Pet {
  val name: String
  val age: Number
}

@Serializable
data class Cat(
  override val name: String,
  override val age: Int,
  val lives: Int = 9,
) : Pet

@Serializable
data class RobotCat(
  override val name: String,
  override val age: Int,
  val id: Id
): Pet {

  // This field helps to simulate encoder to fail half way through
  @Serializable(with = Id.Serializer::class)
  class Id(val value: Long) {
    companion object Serializer : KSerializer<Id> {
      override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Id", PrimitiveKind.LONG)
      override fun deserialize(decoder: Decoder): Id { TODO("Not yet implemented") }
      override fun serialize(encoder: Encoder, value: Id) { TODO("Not yet implemented") }
    }
  }
}

internal val MYLO = Cat(name = "Mylo", age = 1)
internal val OREO = Cat(name = "Oreo", age = 1)
internal val KAT = RobotCat(name = "Kat", age = 12, id = Id(123L))
