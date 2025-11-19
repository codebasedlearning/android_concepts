// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("android-conventions")
}

android {
    namespace = "de.fh_aachen.android.xml_based_app"
    // is set by android-conventions
    // compileSdk = 36

    defaultConfig {
        applicationId = "de.fh_aachen.android.xml_based_app"
        // is set by android-conventions
        // minSdk = 27
        // targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /* all set by android-conventions
    buildTypes {
        release {
            isMinifyEnabled = false     // no shrinking/obfuscation in this course project
            // proguardFiles(           // proguardFiles are only used if isMinifyEnabled = true
            //     getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            // )
        }
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        jvmToolchain(21)
    }
    */
}

dependencies {
// old dependecies
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
implementation("com.google.android.material:material:1.12.0")
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
implementation(libs.androidx.material3)
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.ui.test.junit4)
debugImplementation(libs.androidx.ui.tooling)
debugImplementation(libs.androidx.ui.test.manifest)
}
