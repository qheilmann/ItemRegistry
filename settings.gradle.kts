plugins {
    // Allow automatic download of JDKs if missing
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "itemregistry-root"

include("itemregistry")
include("itemregistry-plugin")

include(":examples:internal:api")
include(":examples:internal-extendable:consumer")
include(":examples:internal-extendable:provider")
include(":examples:shared:consumer")
include(":examples:shared:provider")
