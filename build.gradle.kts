// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Don't actually apply these plugins to the modules, just make them (and their versions)
    // available. This also puts AGP and KGP on the classpath the convention plugin compiles against.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.hilt.android) apply false
}
