// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

import java.util.Properties

/*
 * The database side (SQL scripts, setup steps, a Python tool) is in the folder 'supabase'.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
    alias(libs.plugins.kotlin.serialization)     // the data classes (and navigation keys) are @Serializable
}

// The Supabase settings live in local.properties (not in git), see local.properties.example.
// The file may be missing (fresh checkout, CI), so check before reading it.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localProperty(key: String): String = localProperties.getProperty(key) ?: ""

android {
    namespace = "de.fh_aachen.android.supabase"

    defaultConfig {
        applicationId = "de.fh_aachen.android.supabase"
        versionCode = 1
        versionName = "1.0"

        /*
         * Constants generated into BuildConfig. The URL and the anon key are meant for clients
         * (the anon key is public; row level security protects the data, see supabase/).
         * The user's email and password are only a convenience to pre-fill the sign-in form in
         * this demo - a real app never ships credentials, the user types them.
         */
        buildConfigField("String", "SUPABASE_URL", "\"${localProperty("supabase_url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperty("supabase_anon_key")}\"")
        buildConfigField("String", "SUPABASE_USER_EMAIL", "\"${localProperty("supabase_user_email")}\"")
        buildConfigField("String", "SUPABASE_USER_PASSWORD", "\"${localProperty("supabase_user_password")}\"")
    }

    buildFeatures {
        buildConfig = true      // off by default since AGP 8
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(project(":libs-UiTools"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)

    // Supabase: one BOM, then the modules we use (auth is used directly, so it is listed, too)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    // the HTTP engine for Ktor (used by supabase-kt) and websockets for Realtime
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)

    debugImplementation(libs.androidx.ui.tooling)
}
