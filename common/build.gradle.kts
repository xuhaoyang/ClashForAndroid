plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    compileOnly(project(":hideapi"))

    implementation(kotlin("stdlib-jdk7"))
    implementation(deps.kotlin.coroutine)
    implementation(deps.androidx.core)
}
