// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

/*
 * New concept app: how a Material 3 theme is built and used.
 * On purpose without libs-UiTools: all other concept apps use the shared, 'pure' ConceptTheme;
 * this one shows what goes into a theme of your own.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.theming"

    defaultConfig {
        applicationId = "de.fh_aachen.android.theming"
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
}
