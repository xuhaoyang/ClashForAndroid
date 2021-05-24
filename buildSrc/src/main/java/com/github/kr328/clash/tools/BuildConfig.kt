package com.github.kr328.clash.tools

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import java.io.Serializable

data class BuildConfig(
    val debug: Boolean,
    val premium: Boolean,
    val abis: List<NativeAbi>,
    val minSdkVersion: Int,
) : Serializable {
    companion object {
        fun of(abis: List<NativeAbi>, minSdkVersion: Int, variant: BaseVariant): BuildConfig {
            return BuildConfig(
                debug = variant.buildType.isDebuggable,
                premium = variant.flavorName == "premium",
                abis = abis,
                minSdkVersion = minSdkVersion
            )
        }
    }
}
