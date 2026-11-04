// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

// Starter app: MVVM (DataStore -> Repository -> ViewModel -> Compose) with Navigation 3.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)    // @Serializable NavKeys
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.mvvm_nav3_app"

    defaultConfig {
        applicationId = "de.fh_aachen.android.mvvm_nav3_app"
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
    debugImplementation(libs.androidx.ui.tooling)

    // MVVM
    implementation(libs.androidx.lifecycle.runtime.compose)         // collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)       // viewModel()

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)   // ViewModels per back stack entry
    implementation(libs.kotlinx.serialization.json)                 // rememberNavBackStack

    // Persistence
    implementation(libs.androidx.datastore.preferences)
}
