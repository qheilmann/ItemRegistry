plugins {
    alias(libs.plugins.dotenv)
}

subprojects {
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    tasks {
        withType<JavaCompile>().configureEach {
            options.encoding = Charsets.UTF_8.name()
            options.release = 21
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
