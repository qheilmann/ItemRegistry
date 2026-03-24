plugins {
    `maven-publish`
}

version = (findProperty("apiVersion") as String)

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.adventure.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)

    testImplementation(libs.adventure.api)
    testImplementation(libs.paper.api)
    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.mockito.bom))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.mockbukkit)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("ItemRegistry")
                description.set("ItemRegistry - ItemStack creator registry keyed by Adventure Keys")
                url.set("https://github.com/qheilmann/ItemRegistry")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("qheilmann")
                        name.set("qheilmann")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
