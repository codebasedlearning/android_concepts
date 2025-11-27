plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("android-conventions")
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
    implementation(libs.moshi)

    // Moshi Kotlin extension (for better Kotlin support)
    implementation(libs.moshi.kotlin)

    // Moshi code generation (Kotlin codegen for automatic adapters)
    //kapt("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
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
