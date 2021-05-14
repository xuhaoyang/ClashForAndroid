plugins {
    kotlin("jvm") version "1.5.0"
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(gradleApi())

    api(kotlin("gradle-plugin"))
    api(kotlin("serialization"))
    api("com.android.tools.build:gradle:4.2.1") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    api("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.5.0-1.0.0-alpha10") {
        exclude("com.android.tools.build", "gradle")
    }
}

gradlePlugin {
    plugins {
        create("golang") {
            id = "library-golang"
            implementationClass = "LibraryGolangPlugin"
        }
    }
}
