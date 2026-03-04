plugins {
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.adventure.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
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
