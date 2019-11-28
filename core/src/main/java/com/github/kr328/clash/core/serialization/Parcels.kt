package com.github.kr328.clash.core.serialization

import android.os.Parcel
import com.github.kr328.clash.core.utils.Log
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

object Parcels : AbstractSerialFormat(EmptyModule) {
    fun <T> dump(serializer: SerializationStrategy<T>, obj: T, parcel: Parcel) {
        serializer.serialize(ParcelsEncoder(parcel), obj)
    }

    fun <T> load(deserializer: DeserializationStrategy<T>, parcel: Parcel): T {
        return deserializer.deserialize(ParcelsDecoder(parcel))
    }

    private class ParcelsEncoder(private val parcel: Parcel) :
        Encoder, CompositeEncoder {
        override val context: SerialModule
            get() = EmptyModule

        override fun beginCollection(
            desc: SerialDescriptor,
            collectionSize: Int,
            vararg typeParams: KSerializer<*>
        ): CompositeEncoder {
            encodeInt(collectionSize)
            return super.beginCollection(desc, collectionSize, *typeParams)
        }

        override fun encodeBooleanElement(desc: SerialDescriptor, index: Int, value: Boolean) =
            encodeBoolean(value)
        override fun encodeByteElement(desc: SerialDescriptor, index: Int, value: Byte) =
            encodeByte(value)
        override fun encodeCharElement(desc: SerialDescriptor, index: Int, value: Char) =
            encodeChar(value)
        override fun encodeDoubleElement(desc: SerialDescriptor, index: Int, value: Double) =
            encodeDouble(value)
        override fun encodeFloatElement(desc: SerialDescriptor, index: Int, value: Float) =
            encodeFloat(value)
        override fun encodeIntElement(desc: SerialDescriptor, index: Int, value: Int) =
            encodeInt(value)
        override fun encodeLongElement(desc: SerialDescriptor, index: Int, value: Long) =
            encodeLong(value)
        override fun encodeShortElement(desc: SerialDescriptor, index: Int, value: Short) =
            encodeShort(value)
        override fun encodeStringElement(desc: SerialDescriptor, index: Int, value: String) =
            encodeString(value)
        override fun encodeUnitElement(desc: SerialDescriptor, index: Int) =
            encodeUnit()

        override fun encodeNonSerializableElement(desc: SerialDescriptor, index: Int, value: Any) =
            throw IllegalArgumentException("Unsupported")
        override fun <T : Any> encodeNullableSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) = encodeNullableSerializableValue(serializer, value)
        override fun <T> encodeSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) = encodeSerializableValue(serializer, value)

        override fun beginStructure(
            desc: SerialDescriptor,
            vararg typeParams: KSerializer<*>
        ): CompositeEncoder = this

        override fun encodeBoolean(value: Boolean) =
            parcel.writeByte(if ( value ) 1 else 0)
        override fun encodeByte(value: Byte) =
            parcel.writeByte(value)
        override fun encodeChar(value: Char) =
            parcel.writeInt(value.toInt())
        override fun encodeDouble(value: Double) =
            parcel.writeDouble(value)
        override fun encodeEnum(enumDescription: SerialDescriptor, ordinal: Int) =
            parcel.writeInt(ordinal)
        override fun encodeFloat(value: Float) =
            parcel.writeFloat(value)
        override fun encodeInt(value: Int) =
            parcel.writeInt(value)
        override fun encodeLong(value: Long) =
            parcel.writeLong(value)
        override fun encodeNotNullMark() =
            encodeBoolean(true)
        override fun encodeNull() =
            encodeBoolean(false)
        override fun encodeShort(value: Short) =
            parcel.writeInt(value.toInt())
        override fun encodeString(value: String) =
            parcel.writeString(value)
        override fun encodeUnit() {}
    }

    class ParcelsDecoder(private val parcel: Parcel) : Decoder, CompositeDecoder {
        override val context: SerialModule
            get() = EmptyModule
        override val updateMode: UpdateMode
            get() = UpdateMode.BANNED

        override fun decodeElementIndex(desc: SerialDescriptor) =
            CompositeDecoder.READ_ALL
        override fun decodeCollectionSize(desc: SerialDescriptor) =
            decodeInt()

        override fun decodeBooleanElement(desc: SerialDescriptor, index: Int) =
            decodeBoolean()
        override fun decodeByteElement(desc: SerialDescriptor, index: Int) =
            decodeByte()
        override fun decodeCharElement(desc: SerialDescriptor, index: Int) =
            decodeChar()
        override fun decodeDoubleElement(desc: SerialDescriptor, index: Int) =
            decodeDouble()
        override fun decodeFloatElement(desc: SerialDescriptor, index: Int) =
            decodeFloat()
        override fun decodeIntElement(desc: SerialDescriptor, index: Int) =
            decodeInt()
        override fun decodeShortElement(desc: SerialDescriptor, index: Int) =
            decodeShort()
        override fun decodeLongElement(desc: SerialDescriptor, index: Int) =
            decodeLong()
        override fun decodeStringElement(desc: SerialDescriptor, index: Int) =
            decodeString()
        override fun decodeUnitElement(desc: SerialDescriptor, index: Int) =
            decodeUnit()

        override fun <T : Any> decodeNullableSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T?>
        ) = decodeNullableSerializableValue(deserializer)
        override fun <T> decodeSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>
        ) = decodeSerializableValue(deserializer)

        override fun <T : Any> updateNullableSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T?>,
            old: T?
        ) = updateNullableSerializableValue(deserializer, old)

        override fun <T> updateSerializableElement(
            desc: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>,
            old: T
        ) = updateSerializableValue(deserializer, old)

        override fun beginStructure(
            desc: SerialDescriptor,
            vararg typeParams: KSerializer<*>
        ): CompositeDecoder = this

        override fun decodeBoolean() =
            parcel.readByte() != 0.toByte()
        override fun decodeByte() =
            parcel.readByte()
        override fun decodeChar() =
            parcel.readInt().toChar()
        override fun decodeDouble() =
            parcel.readDouble()
        override fun decodeEnum(enumDescription: SerialDescriptor) =
            parcel.readInt()
        override fun decodeFloat() =
            parcel.readFloat()
        override fun decodeInt() =
            parcel.readInt()
        override fun decodeLong() =
            parcel.readLong()
        override fun decodeNotNullMark() =
            decodeBoolean()
        override fun decodeNull() =
            null
        override fun decodeShort() =
            parcel.readInt().toShort()
        override fun decodeString() =
            parcel.readString() ?: throw NullPointerException("String null")
        override fun decodeUnit() {}
    }
}


