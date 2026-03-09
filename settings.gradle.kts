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

rootProject.name = "MoneyManager"
include(":app")
include(":data_expenses")
include(":domain_expenses")
include(":feature_expenses")

project(":data_expenses").projectDir = file("sources/expenses/data_expenses")
project(":domain_expenses").projectDir = file("sources/expenses/domain_expenses")
project(":feature_expenses").projectDir = file("sources/expenses/feature_expenses")
