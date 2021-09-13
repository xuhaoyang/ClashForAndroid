plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    compileOnly(project(":hideapi"))

    implementation(deps.kotlin.coroutine)
    implementation(deps.androidx.core)
}
