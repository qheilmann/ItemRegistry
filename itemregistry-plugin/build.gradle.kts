plugins {
    alias(libs.plugins.shadow)
}

version = (findProperty("pluginVersion") as String)

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(projects.itemregistry)
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
    implementation(libs.commandapi.shade)
}

tasks {
    shadowJar {
        archiveBaseName.set("itemregistry")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        dependsOn(processResources)
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "apiVersion" to libs.versions.paper.api.get().substringBefore("-R"),
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    register<Copy>("deployJarToServer") {
        group = "deployment"
        description = "Deploy the current built jar to the development server"

        from(shadowJar.flatMap { it.archiveFile })
        into("${env.fetch("SERVER_PATH")}/plugins")

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
