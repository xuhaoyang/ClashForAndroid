@file:Suppress("UNUSED_VARIABLE")

allprojects {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://maven.kr328.app")
        }
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}
