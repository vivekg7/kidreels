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

rootProject.name = "KidReels"

// core: reels, feed rules, settings. Plain Kotlin, no UI, no platform APIs.
include(":core")
// ui: every screen, in Compose Multiplatform. Depends on core only.
include(":ui")
// androidApp: the Android shell (speech, storage, sound). Other platforms get a sibling.
include(":androidApp")
