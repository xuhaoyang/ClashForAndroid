@file:Suppress("UNUSED_VARIABLE")

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import java.net.URL
import java.util.*

buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.kr328.app/releases")
    }
    dependencies {
        classpath(deps.build.android)
        classpath(deps.build.kotlin.common)
        classpath(deps.build.kotlin.serialization)
        classpath(deps.build.ksp)
        classpath(deps.build.golang)
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.kr328.app/releases")
    }
}

subprojects {
    val isApp = name == "app"

    apply(plugin = if (isApp) "com.android.application" else "com.android.library")

    extensions.configure<BaseExtension> {
        val minSdkVersion = 21
        val targetSdkVersion = 30
        val buildVersionCode = 204014
        val buildVersionName = "2.4.14"
        val defaultDimension = "feature"

        ndkVersion = "23.0.7599858"

        compileSdkVersion(targetSdkVersion)

        defaultConfig {
            if (isApp) {
                applicationId = "com.github.kr328.clash"
            }

            minSdk = minSdkVersion
            targetSdk = targetSdkVersion

            versionName = buildVersionName
            versionCode = buildVersionCode

            resValue("string", "release_name", "v$buildVersionName")
            resValue("integer", "release_code", "$buildVersionCode")

            externalNativeBuild {
                cmake {
                    abiFilters("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                }
            }

            if (!isApp) {
                consumerProguardFiles("consumer-rules.pro")
            } else {
                setProperty("archivesBaseName", "cfa-$versionName")
            }
        }

        if (isApp) {
            packagingOptions {
                excludes.add("DebugProbesKt.bin")
            }
        }

        productFlavors {
            flavorDimensions(defaultDimension)

            create("foss") {
                dimension = defaultDimension
                versionNameSuffix = ".foss"

                buildConfigField("boolean", "PREMIUM", "Boolean.parseBoolean(\"false\")")

                if (isApp) {
                    applicationIdSuffix = ".foss"
                }
            }
            create("premium") {
                dimension = defaultDimension
                versionNameSuffix = ".premium"

                buildConfigField("boolean", "PREMIUM", "Boolean.parseBoolean(\"true\")")

                val tracker = rootProject.file("tracker.properties")
                if (tracker.exists()) {
                    val prop = Properties().apply {
                        tracker.inputStream().use(this::load)
                    }

                    buildConfigField(
                        "String",
                        "APP_CENTER_KEY",
                        "\"${prop.getProperty("appcenter.key")!!}\""
                    )
                }
            }
        }

        signingConfigs {
            val keystore = rootProject.file("signing.properties")
            if (keystore.exists()) {
                create("release") {
                    val prop = Properties().apply {
                        keystore.inputStream().use(this::load)
                    }

                    storeFile = rootProject.file(prop.getProperty("keystore.path")!!)
                    storePassword = prop.getProperty("keystore.password")!!
                    keyAlias = prop.getProperty("key.alias")!!
                    keyPassword = prop.getProperty("key.password")!!
                }
            }
        }

        buildTypes {
            named("release") {
                isMinifyEnabled = isApp
                isShrinkResources = isApp
                signingConfig = signingConfigs.findByName("release")
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
            named("debug") {
                versionNameSuffix = ".debug"
            }
        }

        buildFeatures.apply {
            dataBinding {
                isEnabled = name != "hideapi"
            }
        }

        if (isApp) {
            this as AppExtension

            splits {
                abi {
                    isEnable = true
                    isUniversalApk = true
                }
            }
        }
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL

    doLast {
        val sha256 = URL("$distributionUrl.sha256").openStream()
            .use { it.reader().readText().trim() }

        file("gradle/wrapper/gradle-wrapper.properties")
            .appendText("distributionSha256Sum=$sha256")
    }
}