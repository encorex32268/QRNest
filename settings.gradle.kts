rootProject.name = "QRNest"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":androidApp")

include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:presentation")

include(":feature:scan:domain")
include(":feature:scan:data")
include(":feature:scan:presentation")

include(":feature:generate:domain")
include(":feature:generate:data")
include(":feature:generate:presentation")

include(":feature:history:domain")
include(":feature:history:data")
include(":feature:history:presentation")

include(":feature:preview:domain")
include(":feature:preview:data")
include(":feature:preview:presentation")
