// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("android-conventions")               // alias(libs.plugins.android.conventions) does not work, however...
}

android {
    namespace = "de.fh_aachen.android.composable_based_app"
    // is set by android-conventions
    // compileSdk = 36

    defaultConfig {
        applicationId = "de.fh_aachen.android.composable_based_app"
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    // old settings, use jvmToolchain instead
    //    compileOptions {
    //        sourceCompatibility = JavaVersion.VERSION_21
    //        targetCompatibility = JavaVersion.VERSION_21
    //    }
    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true // also set by plugin id("org.jetbrains.kotlin.plugin.compose")
    }
    */

}

dependencies {
    // references libs.versions.toml, without it looks like
    //      implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // 'implementation' controls how and where dependencies are available, but not exposed transitively
    // to other modules that depend on your module; 'api' is like implementation, except that it exposes
    // the dependency transitively to other modules
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // 'implementation(platform(...' BOM (Bill of Materials) '<artifact>-bom' aligns versions from library authors
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // 'implementation(project(...' adds another module from the same Gradle build as a dependency
    //implementation(project(":Unit0x00:B_Widgets"))
    //implementation(project(":Unit0x00:C_Db"))
    testImplementation(libs.junit)
    // 'androidTestImplementation' available only when compiling/running unit tests, not included in the final app
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // 'debugImplementation' only exists in the debug build variant
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
