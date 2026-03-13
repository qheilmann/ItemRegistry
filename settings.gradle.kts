plugins {
    // Allow automatic download of JDKs if missing
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ItemRegistry"

include("itemregistry")

include(":examples:api-only-example")
include(":examples:consumer-plugin-example")
include(":examples:producer-plugin-example")
