package com.github.kr328.clash.core.event

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.StringDescriptor
import java.lang.IllegalArgumentException

@Serializable
data class LogEvent(val level: Level, val message: String):
    Event {
    companion object {
        const val DEBUG_VALUE = 1
        const val INFO_VALUE = 2
        const val WARN_VALUE = 3
        const val ERROR_VALUE = 4
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
}