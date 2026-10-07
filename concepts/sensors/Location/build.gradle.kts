// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
}

// The Maps API key lives in local.properties (not in git), see local.properties.example.
// The file may be missing (fresh checkout, CI), so check before reading it.
val mapsApiKey: String = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}.getProperty("google_api_key") ?: ""

android {
    namespace = "de.fh_aachen.android.location"

    defaultConfig {
        applicationId = "de.fh_aachen.android.location"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // this time not with buildConfigField, but as a manifest placeholder, see AndroidManifest.xml
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(project(":libs-UiTools"))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
