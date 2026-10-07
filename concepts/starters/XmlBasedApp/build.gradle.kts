// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // no Compose compiler plugin: this starter is the 'old way' with XML layouts and Views only
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.xml_based_app"
    // is set by fhac.android.conventions
    // compileSdk = 37

    defaultConfig {
        applicationId = "de.fh_aachen.android.xml_based_app"
        // is set by fhac.android.conventions
        // minSdk = 27
        // targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /* all set by fhac.android.conventions
    buildTypes {
        release {
            isMinifyEnabled = false     // no shrinking/obfuscation in this course project
            // proguardFiles(           // proguardFiles are only used if isMinifyEnabled = true
            //     getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            // )
        }
    }

    kotlin {
        jvmToolchain(21)
    }
    */
}

dependencies {
    // the 'View world': AppCompat, ConstraintLayout and Material Components (XML themes and widgets)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
