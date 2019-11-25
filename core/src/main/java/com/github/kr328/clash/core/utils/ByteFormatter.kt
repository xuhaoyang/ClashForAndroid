package com.github.kr328.clash.core.utils

object ByteFormatter {
    fun byteToString(bytes: Long): String {
        return when {
            bytes > 1024 * 1024 * 1024 ->
                (bytes / 1024 / 1024 / 1024).toString() + " GiB"
            bytes > 1024 * 1024 ->
                (bytes / 1024 / 1024).toString() + " MiB"
            bytes > 1024 ->
                (bytes / 1024).toString() + " KiB"
            else ->
                "$bytes Bytes"
        }
    }
}