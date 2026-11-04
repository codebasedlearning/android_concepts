// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

import java.util.Properties

// MVVM app on Supabase: login, catalog (master/detail), pictures, chat. Database: python_supabase.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)    // data classes and NavKeys
    alias(libs.plugins.fhac.android.conventions)
}

// URL and key live in local.properties (not in git), see local.properties.example.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localProperty(key: String): String = localProperties.getProperty(key) ?: ""

android {
    namespace = "de.fh_aachen.android.mvvm_supabase_app"

    defaultConfig {
        applicationId = "de.fh_aachen.android.mvvm_supabase_app"
        versionCode = 1
        versionName = "1.0"

        // URL and publishable key are meant for clients; the database border protects the data.
        buildConfigField("String", "SUPABASE_URL", "\"${localProperty("mobapp_supabase_url")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperty("mobapp_supabase_key")}\"")
        // optional: pre-fills the e-mail field
        buildConfigField("String", "SUPABASE_USER_EMAIL", "\"${localProperty("mobapp_user_email")}\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Supabase (the BOM aligns the module versions) + Ktor engine and websockets (Realtime)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)

    // Images from URLs
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
