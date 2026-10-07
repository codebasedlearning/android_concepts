// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.room"

    defaultConfig {
        applicationId = "de.fh_aachen.android.room"
        versionCode = 1
        versionName = "1.0"
    }
}

// Room Gradle plugin: exports the schema of every database version as JSON into 'schemas/'.
// Commit these files; they are the basis for migrations and AutoMigrations.
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(project(":libs-UiTools"))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
