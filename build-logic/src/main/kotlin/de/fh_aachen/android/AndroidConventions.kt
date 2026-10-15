// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/*
 * Convention plugin 'fhac.android.conventions', applied in every module via
 *      alias(libs.plugins.fhac.android.conventions)
 *
 * It sets compileSdk, minSdk, targetSdk and the JVM toolchain for all apps and libs.
 * The values come from gradle.properties ('fhac.*'); the numbers below are only fallbacks
 * if a property is missing - gradle.properties always wins.
 */
class AndroidConventions : Plugin<Project> {
    override fun apply(project: Project) {

        // findProperty also looks into the root project's gradle.properties
        fun intProp(key: String, default: Int) =
            (project.findProperty(key) ?: default).toString().toInt()

        val compileSdk   = intProp("fhac.compileSdk", 37)
        val minSdk       = intProp("fhac.minSdk", 27)
        val targetSdk    = intProp("fhac.targetSdk", compileSdk)
        val jvmToolchain = intProp("fhac.jvmToolchain", 25)

        project.plugins.withId("com.android.application") {
            project.extensions.configure<ApplicationExtension> {
                this.compileSdk = compileSdk
                defaultConfig {
                    this.minSdk = minSdk
                    this.targetSdk = targetSdk
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false     // no shrinking/obfuscation in this course project
                    }
                }
            }
        }

        project.plugins.withId("com.android.library") {
            project.extensions.configure<LibraryExtension> {
                this.compileSdk = compileSdk
                defaultConfig {
                    this.minSdk = minSdk
                }
            }
        }

        // AGP 9 'built-in Kotlin': there is no kotlin-android plugin any more, AGP registers
        // the 'kotlin' extension itself, so we hook on the Android plugins instead.
        listOf("com.android.application", "com.android.library").forEach { id ->
            project.plugins.withId(id) {
                val kotlin = project.extensions.findByType(KotlinProjectExtension::class.java)
                if (kotlin != null) kotlin.jvmToolchain(jvmToolchain)
                else project.logger.warn("fhac: no 'kotlin' extension in ${project.path}, jvmToolchain not set")
            }
        }

        // Compose only where the Compose compiler plugin is applied (the XML starter has none).
        project.plugins.withId("org.jetbrains.kotlin.plugin.compose") {
            project.plugins.withId("com.android.application") {
                project.extensions.configure<ApplicationExtension> { buildFeatures { compose = true } }
            }
            project.plugins.withId("com.android.library") {
                project.extensions.configure<LibraryExtension> { buildFeatures { compose = true } }
            }
        }

    }
}
