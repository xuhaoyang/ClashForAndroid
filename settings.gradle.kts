@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "ClashForAndroid"

include(":app")
include(":core")
include(":service")
include(":design")
include(":common")
include(":hideapi")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            val agp = "7.0.3"
            val ksp = "1.5.31-1.0.0"
            val kotlin = "1.5.31"
            val golang = "1.0.4"
            val coroutine = "1.5.2"
            val coreKtx = "1.6.0"
            val activity = "1.3.1"
            val fragment = "1.3.6"
            val appcompat = "1.3.1"
            val coordinator = "1.1.0"
            val recyclerview = "1.2.1"
            val viewpager = "1.0.0"
            val material = "1.4.0"
            val appcenter = "4.3.1"
            val serialization = "1.3.0"
            val kaidl = "1.15"
            val room = "2.3.0"
            val multiprocess = "1.0.0"

            alias("build-android").to("com.android.tools.build:gradle:$agp")
            alias("build-kotlin-common").to("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
            alias("build-kotlin-serialization").to("org.jetbrains.kotlin:kotlin-serialization:$kotlin")
            alias("build-ksp").to("com.google.devtools.ksp:symbol-processing-gradle-plugin:$ksp")
            alias("build-golang").to("com.github.kr328.golang:gradle-plugin:$golang")
            alias("kotlin-coroutine").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine")
            alias("kotlin-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
            alias("androidx-core").to("androidx.core:core-ktx:$coreKtx")
            alias("androidx-activity").to("androidx.activity:activity:$activity")
            alias("androidx-fragment").to("androidx.fragment:fragment:$fragment")
            alias("androidx-appcompat").to("androidx.appcompat:appcompat:$appcompat")
            alias("androidx-coordinator").to("androidx.coordinatorlayout:coordinatorlayout:$coordinator")
            alias("androidx-recyclerview").to("androidx.recyclerview:recyclerview:$recyclerview")
            alias("androidx-viewpager").to("androidx.viewpager2:viewpager2:$viewpager")
            alias("androidx-room-compiler").to("androidx.room:room-compiler:$room")
            alias("androidx-room-runtime").to("androidx.room:room-runtime:$room")
            alias("androidx-room-ktx").to("androidx.room:room-ktx:$room")
            alias("google-material").to("com.google.android.material:material:$material")
            alias("appcenter-analytics").to("com.microsoft.appcenter:appcenter-analytics:$appcenter")
            alias("appcenter-crashes").to("com.microsoft.appcenter:appcenter-crashes:$appcenter")
            alias("kaidl-compiler").to("com.github.kr328.kaidl:kaidl:$kaidl")
            alias("kaidl-runtime").to("com.github.kr328.kaidl:kaidl-runtime:$kaidl")
            alias("rikkax-multiprocess").to("dev.rikka.rikkax.preference:multiprocess:$multiprocess")
        }
    }
}