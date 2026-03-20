pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "databaseManagerWorkSpace"

include(":app")
include(":core:database-operations")
include(":core:android-stub")
