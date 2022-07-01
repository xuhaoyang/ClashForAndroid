rootProject.name = "ClashForAndroid"

include(":app")
include(":core")
include(":service")
include(":design")
include(":common")
include(":hideapi")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val agp = "7.2.1"
            val kotlin = "1.7.0"
            val ksp = "$kotlin-1.0.6"
            val golang = "1.0.4"
            val coroutine = "1.6.3"
            val coreKtx = "1.8.0"
            val activity = "1.5.0"
            val fragment = "1.5.0"
            val appcompat = "1.4.2"
            val coordinator = "1.2.0"
            val recyclerview = "1.2.1"
            val viewpager = "1.0.0"
            val material = "1.6.1"
            val serialization = "1.3.3"
            val kaidl = "1.15"
            val room = "2.4.2"
            val multiprocess = "1.0.0"

            library("build-android", "com.android.tools.build:gradle:$agp")
            library("build-kotlin-common", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
            library("build-kotlin-serialization", "org.jetbrains.kotlin:kotlin-serialization:$kotlin")
            library("build-ksp", "com.google.devtools.ksp:symbol-processing-gradle-plugin:$ksp")
            library("build-golang", "com.github.kr328.golang:gradle-plugin:$golang")
            library("kotlin-coroutine", "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine")
            library("kotlin-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
            library("androidx-core", "androidx.core:core-ktx:$coreKtx")
            library("androidx-activity", "androidx.activity:activity:$activity")
            library("androidx-fragment", "androidx.fragment:fragment:$fragment")
            library("androidx-appcompat", "androidx.appcompat:appcompat:$appcompat")
            library("androidx-coordinator", "androidx.coordinatorlayout:coordinatorlayout:$coordinator")
            library("androidx-recyclerview", "androidx.recyclerview:recyclerview:$recyclerview")
            library("androidx-viewpager", "androidx.viewpager2:viewpager2:$viewpager")
            library("androidx-room-compiler", "androidx.room:room-compiler:$room")
            library("androidx-room-runtime", "androidx.room:room-runtime:$room")
            library("androidx-room-ktx", "androidx.room:room-ktx:$room")
            library("google-material", "com.google.android.material:material:$material")
            library("kaidl-compiler", "com.github.kr328.kaidl:kaidl:$kaidl")
            library("kaidl-runtime", "com.github.kr328.kaidl:kaidl-runtime:$kaidl")
            library("rikkax-multiprocess", "dev.rikka.rikkax.preference:multiprocess:$multiprocess")
        }
    }
}