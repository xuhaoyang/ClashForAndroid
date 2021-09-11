plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":service"))
    implementation(project(":design"))
    implementation(project(":common"))

    implementation(kotlin("stdlib-jdk7"))
    implementation(deps.kotlin.coroutine)
    implementation(deps.androidx.core)
    implementation(deps.androidx.activity)
    implementation(deps.androidx.fragment)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.coordinator)
    implementation(deps.androidx.recyclerview)
    implementation(deps.google.material)

    val premiumImplementation by configurations

    premiumImplementation(deps.appcenter.analytics)
    premiumImplementation(deps.appcenter.crashes)
}

tasks.getByName("clean", type = Delete::class) {
    delete(file("release"))
}
