// Minimal ".env" reader for deployment tasks
// Values from the environment take precedence over the ".env" file.
// Exposed to subprojects via rootProject.extra
val dotEnv: Map<String, String> = rootProject.file(".env").let { file ->
    if (!file.exists()) {
        emptyMap()
    } else {
        file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .associate { line ->
                val (key, value) = line.split("=", limit = 2)
                key.trim() to value.trim().trim('"', '\'')
            }
    }
}

fun envFetchOrNull(key: String): String? = System.getenv(key) ?: dotEnv[key]

fun envFetch(key: String): String = envFetchOrNull(key)
    ?: throw GradleException("Missing required environment variable \"$key\" (set it in the environment or in .env)")

extra["envFetch"] = ::envFetch
extra["envFetchOrNull"] = ::envFetchOrNull

tasks {
    register("shadowJarAndDeployExampleInternalOnly") {
        group = "deployment"
        description = "Build and deploy the internal-only example plugin"

        dependsOn(":examples:internal:api:shadowJarAndDeploy")
    }

    register("shadowJarAndDeployExampleInternalExtendable") {
        group = "deployment"
        description = "Build and deploy internal-extendable consumer and provider example plugins"

        dependsOn(":examples:internal-extendable:consumer:shadowJarAndDeploy")
        dependsOn(":examples:internal-extendable:provider:shadowJarAndDeploy")
    }

    register("shadowJarAndDeployExampleShared") {
        group = "deployment"
        description = "Build and deploy ItemRegistry runtime plugin plus shared provider and consumer examples"

        dependsOn(":itemregistry-plugin:shadowJarAndDeploy")
        dependsOn(":examples:shared:provider:shadowJarAndDeploy")
        dependsOn(":examples:shared:consumer:shadowJarAndDeploy")
    }
}

subprojects {
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    tasks {
        withType<JavaCompile>().configureEach {
            options.encoding = Charsets.UTF_8.name()
            options.release = 25
            options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
        }

        withType<Javadoc>().configureEach {
            options.encoding = Charsets.UTF_8.name()
        }

        withType<ProcessResources>().configureEach {
            filteringCharset = Charsets.UTF_8.name()
        }

        withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
