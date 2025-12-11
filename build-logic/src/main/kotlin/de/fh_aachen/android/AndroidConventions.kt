// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidConventions : Plugin<Project> {
    override fun apply(project: Project) {

        // from gradle.properties
        fun intProp(key: String, default: Int) =
            (project.findProperty(key) ?: project.rootProject.findProperty(key) ?: default)
                .toString().toInt()

        // for all projects and libs set these
        val compileSdk   = intProp("android.compileSdk", 36)
        val minSdk       = intProp("android.minSdk", 29)
        val targetSdk    = intProp("android.targetSdk", compileSdk)
        val jvmToolchain = intProp("kotlin.android.jvmToolchain", 21)

        project.plugins.withId("com.android.application") {
            project.extensions.configure<ApplicationExtension> {
                this.compileSdk = compileSdk
                defaultConfig {
                    this.minSdk = minSdk
                    this.targetSdk = targetSdk
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }
                buildFeatures { // also set by plugin id("org.jetbrains.kotlin.plugin.compose")
                    compose = true
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

        project.plugins.withId("org.jetbrains.kotlin.android") {
            project.extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
                jvmToolchain(jvmToolchain)
            }
        }

    }
}
