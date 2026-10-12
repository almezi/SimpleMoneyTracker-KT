// STAGE 1 SPIKE — throwaway project. Answers one question:
// do Compose Multiplatform testTags surface as iOS accessibilityIdentifiers?
// Independent Gradle build, deliberately NOT part of the Saku build.

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "cmp-spike"
include(":composeApp")