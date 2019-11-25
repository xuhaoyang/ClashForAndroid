package com.github.kr328.clash.core.event

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class LogEvent(val level: Level, val message: String):
    Event, Parcelable {
    companion object {
        const val DEBUG_VALUE = 1
        const val INFO_VALUE = 2
        const val WARN_VALUE = 3
        const val ERROR_VALUE = 4

        @JvmField
        val CREATOR = object: Parcelable.Creator<LogEvent> {
            override fun createFromParcel(parcel: Parcel): LogEvent {
                return Parcels.load(serializer(), parcel)
            }

            override fun newArray(size: Int): Array<LogEvent?> {
                return arrayOfNulls(size)
            }
        }
    }

    @Serializable(LevelSerializer::class)
    enum class Level(val value: Int) {
        DEBUG(DEBUG_VALUE), INFO(
            INFO_VALUE
        ), WARN(WARN_VALUE), ERROR(
            ERROR_VALUE
        )
    }

    class LevelSerializer : KSerializer<Level> {
        override val descriptor: SerialDescriptor
            get() = StringDescriptor

        override fun deserialize(decoder: Decoder): Level {
            return when (val value = decoder.decodeInt()) {
                DEBUG_VALUE -> Level.DEBUG
                INFO_VALUE -> Level.INFO
                WARN_VALUE -> Level.WARN
                ERROR_VALUE -> Level.ERROR
                else -> throw IllegalArgumentException("Invalid level type $value")
            }
        }

        override fun serialize(encoder: Encoder, obj: Level) {
            encoder.encodeInt(obj.value)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }
}