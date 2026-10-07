// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.ui_tools"
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

    // 'api' instead of 'implementation': our public API exposes navigation types
    // (LocalNavController: NavController), so every app using UiTools needs them, too.
    api(libs.androidx.navigation.compose)
}
