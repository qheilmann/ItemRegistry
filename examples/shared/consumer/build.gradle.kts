val pluginVersion = "1.0.0"

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(projects.itemregistry)
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
}

tasks {
    shadowJar {
        archiveBaseName.set("itemregistry-shared-consumer-example")
        archiveVersion.set(pluginVersion)
        archiveClassifier.set("")
        dependsOn(processResources)
    }

    processResources {
        val props = mapOf(
            "version" to pluginVersion,
            "apiVersion" to providers.gradleProperty("paperApiVersion").get(),
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    register<Copy>("deployJarToServer") {
        group = "deployment"
        description = "Deploy the current built jar to the development server"

        val envFetch: (String) -> String by rootProject.extra

        from(shadowJar.flatMap { it.archiveFile })
        into("${envFetch("SERVER_PATH")}/plugins")

        val baseNameProvider = shadowJar.flatMap { it.archiveBaseName }

        doFirst {
            val baseName = baseNameProvider.get()
            destinationDir.listFiles()
                ?.filter { it.name.matches(Regex("^${Regex.escape(baseName)}-[0-9][0-9.\\-]*\\.jar$")) }
                ?.forEach { file ->
                    logger.lifecycle("Cleaning old ${baseName} plugin files: ${file.name}")
                    file.delete()
                }
            logger.lifecycle("Deploy ${baseName} to ${destinationDir.absolutePath} ...")
        }
    }

    register("shadowJarAndDeploy") {
        group = "deployment"
        description = "Build shadow jar and deploy to the development server"

        dependsOn(shadowJar)
        finalizedBy(named("deployJarToServer"))
    }
}
