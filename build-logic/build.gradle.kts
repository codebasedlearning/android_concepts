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
    // 'compileOnly': we only compile against AGP and KGP. At runtime the versions applied in the
    // root build.gradle.kts are used, so libs.versions.toml decides them in exactly one place
    // (same pattern as in Google's "Now in Android" sample).
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

gradlePlugin {
    plugins {
        create("androidConventions") {
            // namespaced id; modules use it via alias(libs.plugins.fhac.android.conventions)
            id = "fhac.android.conventions"
            implementationClass = "de.fh_aachen.android.AndroidConventions"
        }
    }
}
