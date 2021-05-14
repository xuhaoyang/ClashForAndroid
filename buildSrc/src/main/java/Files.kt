import org.gradle.api.Project
import java.io.File

val Project.golangSource: File
    get() = file("src/main/golang")

val Project.golangBuild: File
    get() = buildDir.resolve("intermediates/golang")
