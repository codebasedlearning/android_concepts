// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    alias(libs.plugins.fhac.android.conventions)
}
//https://developer.android.com/build/migrate-to-ksp

android {
    namespace = "de.fh_aachen.android.rest"

    defaultConfig {
        applicationId = "de.fh_aachen.android.rest"
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.moshi)

    // Moshi code generation: KSP generates an adapter for every @JsonClass(generateAdapter = true).
    // Alternative: 'moshi-kotlin' with KotlinJsonAdapterFactory (reflection, pulls in kotlin-reflect).
    // Use one of the two, not both.
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)

    // Retrofit Moshi converter
    implementation(libs.converter.moshi)

    // Optional: OkHttp for advanced networking and logging
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //kapt(libs.androidx.room.compiler)
    implementation(project(":libs-UiTools"))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
