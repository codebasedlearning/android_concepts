// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    `kotlin-dsl` // write Gradle plugins in Kotlin
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    //implementation("com.android.tools.build:gradle:8.13.0")
    // use the AGP version from libs.versions.toml
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation(kotlin("gradle-plugin", embeddedKotlinVersion))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

gradlePlugin {
    plugins {
        // one way to introduce the plugin
        create("androidConventions") {
            id = "android-conventions"
            implementationClass = "de.fh_aachen.android.AndroidConventions"
            version = "0.1.0"
        }
    }
}
