// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "de.fh_aachen.android.navigation_v3_drawer"

    defaultConfig {
        applicationId = "de.fh_aachen.android.navigation_v3_drawer"
        versionCode = 1
        versionName = "1.0"
    }
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
    implementation(project(":libs-UiTools"))

    debugImplementation(libs.androidx.ui.tooling)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // For rememberNavBackStack (uses Kotlin serialization)
    implementation(libs.kotlinx.serialization.json)
}
