plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":service"))

    implementation(kotlin("stdlib-jdk7"))
    implementation(deps.kotlin.coroutine)
    implementation(deps.androidx.core)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.activity)
    implementation(deps.androidx.coordinator)
    implementation(deps.androidx.recyclerview)
    implementation(deps.androidx.fragment)
    implementation(deps.androidx.viewpager)
    implementation(deps.google.material)
}
