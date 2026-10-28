// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    alias(libs.plugins.fhac.android.conventions)
    alias(libs.plugins.kotlin.serialization)     // for the @Serializable navigation keys
}

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

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)     // rememberNavBackStack saves the keys
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
}
