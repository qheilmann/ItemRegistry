plugins {
    // Allow automatic download of JDKs if missing
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ItemRegistry"

include("itemregistry")
include("itemregistry-playground")
