import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = buildTargetSdkVersion

    flavorDimensions(buildFlavor)

    defaultConfig {
        applicationId = "com.github.kr328.clash"

        minSdk = buildMinSdkVersion
        targetSdk = buildTargetSdkVersion

        versionCode = buildVersionCode
        versionName = buildVersionName

        resConfigs("zh-rCN", "zh-rHK", "zh-rTW")

        resValue("string", "release_name", "v$buildVersionName")
        resValue("integer", "release_code", "$buildVersionCode")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    productFlavors {
        create("open") {
            dimension = "open"
            versionNameSuffix = ".open-source"
        }
        create("premium") {
            dimension = "premium"
            versionNameSuffix = ".premium"
        }
    }

    val signingFile = rootProject.file("keystore.properties")
    if (signingFile.exists()) {
        val properties = Properties().apply {
            signingFile.inputStream().use {
                load(it)
            }
        }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(properties.getProperty("storeFile")!!)
                storePassword = properties.getProperty("storePassword")!!
                keyAlias = properties.getProperty("keyAlias")!!
                keyPassword = properties.getProperty("keyPassword")!!
            }
        }
        buildTypes {
            named("release") {
                signingConfig = signingConfigs["release"]
            }
        }
    }

    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = true
        }
    }

    buildTypes.apply {
        val properties = Properties().apply {
            rootProject.file("local.properties").inputStream().use {
                load(it)
            }
        }

        val key = properties.getProperty("appcenter.key", null)

        forEach {
            if (it.name == "debug" || key == null) {
                it.buildConfigField("String", "APP_CENTER_KEY", "null")
            } else {
                it.buildConfigField("String", "APP_CENTER_KEY", "\"$key\"")
            }
        }
    }
}

dependencies {
    api(project(":core"))
    api(project(":service"))
    api(project(":design"))
    api(project(":common"))

    implementation(kotlin("stdlib-jdk7"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("androidx.core:core-ktx:$ktxVersion")
    implementation("androidx.activity:activity:$activityVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("androidx.coordinatorlayout:coordinatorlayout:$coordinatorlayoutVersion")
    implementation("androidx.recyclerview:recyclerview:$recyclerviewVersion")
    implementation("androidx.fragment:fragment:$fragmentVersion")
    implementation("com.microsoft.appcenter:appcenter-analytics:$appcenterVersion")
    implementation("com.microsoft.appcenter:appcenter-crashes:$appcenterVersion")
    implementation("com.google.android.material:material:$materialVersion")
}

task("cleanRelease", type = Delete::class) {
    delete(file("release"))
}

afterEvaluate {
    tasks["clean"].dependsOn(tasks["cleanRelease"])
}