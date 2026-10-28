plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "de.fh_aachen.android.photo1"
    defaultConfig {
        applicationId = "de.fh_aachen.android.photo1"
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)     // rememberNavBackStack saves the keys
    implementation(project(":libs-UiTools"))
    debugImplementation(libs.androidx.ui.tooling)

    // Coil 3 (group io.coil-kt.coil3, package coil3); content:// and file URIs work without a network artifact
    implementation(libs.coil.compose)
}
