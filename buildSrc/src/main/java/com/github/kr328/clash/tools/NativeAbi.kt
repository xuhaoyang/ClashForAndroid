package com.github.kr328.clash.tools

enum class NativeAbi(
    val value: String,
    val compiler: String,
    val goArch: String,
    val goArm: String
) {
    ArmeabiV7a("armeabi-v7a", "armv7a-linux-androideabi", "arm", "7"),
    Arm64V8a("arm64-v8a", "aarch64-linux-android", "arm64", ""),
    X86("x86", "i686-linux-android", "386", ""),
    X64("x86_64", "x86_64-linux-android", "amd64", "");

    companion object {
        fun parse(value: String): NativeAbi {
            return when (value) {
                ArmeabiV7a.value -> ArmeabiV7a
                Arm64V8a.value -> Arm64V8a
                X86.value -> X86
                X64.value -> X64
                else -> throw IllegalArgumentException("unsupported abi $value")
            }
        }
    }
}
