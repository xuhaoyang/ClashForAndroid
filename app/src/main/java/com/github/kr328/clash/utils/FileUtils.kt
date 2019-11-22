package com.github.kr328.clash.utils

import java.io.File
import java.security.SecureRandom

object FileUtils {
    private val random = SecureRandom()

    fun generateRandomFile(dir: File, suffix: String = ""): File {
        dir.mkdirs()

        var file: File

        do {
            file = dir.resolve(random.nextLong().toString() + suffix)
        } while ( file.exists() )

        return file
    }
}