package com.github.kr328.clash.core.utils

object ByteFormatter {
    fun byteToString(bytes: Long): String {
        return when {
            bytes > 1024 * 1024 * 1024 ->
                String.format("%.2f GB", (bytes.toDouble() / 1024 / 1024 / 1024))
            bytes > 1024 * 1024 ->
                String.format("%.2f MB", (bytes.toDouble() / 1024 / 1024))
            bytes > 1024 ->
                String.format("%.2f KB", (bytes.toDouble() / 1024))
            else ->
                "$bytes Byte"
        }
    }

    fun byteToStringSecond(bytes: Long): String {
        return byteToString(bytes) + "/s"
    }
}