package com.github.kr328.clash.core.serialization

import android.os.Parcel
import com.github.kr328.clash.core.utils.Log
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule

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

    class ParcelsDecoder(private val parcel: Parcel) : ElementValueDecoder() {
        private var nextObject: Any? = null

        override fun decodeNotNullMark(): Boolean {
            nextObject = parcel.readValue(Parcels::class.java.classLoader)

            Log.i("-> $nextObject")

            return nextObject != null
        }

        override fun decodeNull(): Nothing? = null

        override fun decodeValue(): Any {
            return nextObject ?: parcel.readValue(Parcels::class.java.classLoader)
            ?: throw NullPointerException("Parcel read data null")
        }
    }
}


