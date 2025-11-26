plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("android-conventions")

    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "de.fh_aachen.android.navigation_v3"

    defaultConfig {
        applicationId = "de.fh_aachen.android.navigation_v3"
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
    //implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    //implementation("androidx.compose.material:material-icons-extended:1.13.0")
    //implementation("androidx.compose.material:material:1.9.3")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(project(":libs-UiTools"))

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui.android)

    // For rememberNavBackStack (uses Kotlin serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material3)
}
