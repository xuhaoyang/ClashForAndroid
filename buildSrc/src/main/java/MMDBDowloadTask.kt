import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

open class MMDBDowloadTask : DefaultTask() {
    companion object {
        const val URL = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.tar.gz"
    }

    var output: String = ""

    @TaskAction
    fun exec() {
        val file = File(output).apply {
            parentFile?.mkdirs()
        }

        try {
            (URL(URL).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true

                connect()
                require(responseCode / 100 == 2)

                FileOutputStream(file).use {
                    inputStream.copyTo(it)
                }

                disconnect()
            }
        }
        catch (e: Throwable) {
            e.printStackTrace()
            throw GradleException("Download failure", e)
        }
    }
}