import org.gradle.api.GradleException
import org.gradle.api.Project

// Minimal ".env" reader for deployment tasks (replaces the unmaintained co.uzzu.dotenv.gradle plugin).
// Values from the environment take precedence over the ".env" file. Lives in buildSrc so it's
// callable as a typed extension function from every subproject's build script, without going
// through Gradle's untyped `extra` property bag.
private val dotEnvCache = mutableMapOf<Project, Map<String, String>>()

private fun dotEnv(project: Project): Map<String, String> = dotEnvCache.getOrPut(project.rootProject) {
    project.rootProject.file(".env").let { file ->
        if (!file.exists()) {
            emptyMap()
        } else {
            file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                .associate { line ->
                    val (key, value) = line.split("=", limit = 2)
                    key.trim() to value.trim().trim('"').trim('\'')
                }
        }
    }
}

fun Project.envFetchOrNull(key: String): String? = System.getenv(key) ?: dotEnv(this)[key]

fun Project.envFetch(key: String): String = envFetchOrNull(key)
    ?: throw GradleException("Missing required environment variable \"$key\" (set it in the environment or in .env)")
