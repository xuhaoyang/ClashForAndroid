plugins {
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("com.android.library")
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(deps.kaidl.compiler)
    kapt(deps.androidx.room.compiler)

    implementation(project(":core"))
    implementation(project(":common"))

    implementation(deps.kotlin.coroutine)
    implementation(deps.kotlin.serialization.json)
    implementation(deps.androidx.core)
    implementation(deps.androidx.room.runtime)
    implementation(deps.androidx.room.ktx)
    implementation(deps.kaidl.runtime)
    implementation(deps.rikkax.multiprocess)
}

afterEvaluate {
    android {
        libraryVariants.forEach {
            sourceSets[it.name].java.srcDir(buildDir.resolve("generated/ksp/${it.name}/kotlin"))
        }
    }
}