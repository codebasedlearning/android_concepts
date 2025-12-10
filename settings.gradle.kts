// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

import kotlin.io.path.*

// This sets 'android.compileSdk', 'android.defaultConfig.minSdk' and 'android.defaultConfig.targetSdk' for all projects and libs.
includeBuild("build-logic")

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

rootProject.name = "Android-Concepts"

fun includeConcepts(vararg unitRoots: String) {
    unitRoots.forEach { root ->
        val rootPath = file("concepts/$root").toPath()
        if (!rootPath.exists() || !rootPath.isDirectory()) return@forEach
        rootPath.toFile().listFiles { f -> f.isDirectory }?.forEach { modDir ->
            val hasBuild = modDir.toPath().resolve("build.gradle.kts").exists() ||
                    modDir.toPath().resolve("build.gradle").exists()
            if (hasBuild) {
                val gradlePath = ":$root-${modDir.name}"
                println("found: $gradlePath")
                include(gradlePath)
                project(gradlePath).projectDir = modDir
            }
        }
    }
}

includeConcepts(
    "starters", "composables", "navigation", "permissions",
    "rest", "sensors", "database", "models", "preferences",
    "libs"
)

/*
 * For simple modules we usually have
 *      include(":FancyApp", ":Widgets", ":Db")
 *
 * In a project with folders we can use
 *      include(":FancyApp", ":Widgets", ":Db")
 *      project(":FancyApp").projectDir = file("Unit0x00/FancyApp")
 *      ...
 * This is basically what includeConcepts does, it collects modules in folders.
 */
