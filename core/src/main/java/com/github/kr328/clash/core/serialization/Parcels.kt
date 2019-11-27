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
        ElementValueEncoder() {
        override fun encodeValue(value: Any) {
            parcel.writeValue(value)
        }

        override fun encodeNull() {
            parcel.writeValue(null)
        }
    }

    class ParcelsDecoder(private val parcel: Parcel) : Decoder, CompositeDecoder {
        override val context: SerialModule
            get() = EmptyModule
        override val updateMode: UpdateMode
            get() = UpdateMode.BANNED

        override fun decodeElementIndex(desc: SerialDescriptor) =
            CompositeDecoder.READ_ALL

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

        override fun decodeBoolean(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeByte(): Byte {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeChar(): Char {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeDouble(): Double {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeEnum(enumDescription: SerialDescriptor): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeFloat(): Float {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeInt(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeLong(): Long {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeNotNullMark(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeNull(): Nothing? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeShort(): Short {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun decodeUnit() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}


