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
        // Add explicit Maven repository as fallback
        maven {
            url = uri("https://repo1.maven.org/maven2/")
            isAllowInsecureProtocol = false
        }
    }
}

rootProject.name = "Octane"
include(":app")
