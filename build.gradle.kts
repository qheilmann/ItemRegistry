
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
