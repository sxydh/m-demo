pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidQsDemo"
include(":app")
include(":surfaceviewdemo")
include(":motioneventdemo")
include(":recyclerviewdemo")
include(":layoutdemo")
