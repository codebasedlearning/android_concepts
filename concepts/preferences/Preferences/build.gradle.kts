plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.preferences"

    defaultConfig {
        applicationId = "de.fh_aachen.android.preferences"
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
    //implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(project(":libs-UiTools"))

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.datastore.preferences)

}
