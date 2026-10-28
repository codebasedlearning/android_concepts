// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

/*
 * Where tests live (source sets):
 *  - src/test         Local unit tests: run on the JVM of your computer, no device, fast.
 *                     Right for pure logic and ViewModels (with fakes).      -> 'testImplementation'
 *  - src/androidTest  Instrumented tests: run on a device or emulator, inside the app.
 *                     Needed for UI (Compose) and anything that needs Android. -> 'androidTestImplementation'
 * Run them: right-click the test class/folder in Android Studio, or
 *      ./gradlew :testing-Testing:testDebugUnitTest            (local)
 *      ./gradlew :testing-Testing:connectedDebugAndroidTest    (device or emulator needed)
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.testing"

    defaultConfig {
        applicationId = "de.fh_aachen.android.testing"
        versionCode = 1
        versionName = "1.0"
        // the runner that starts the instrumented tests on the device
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // local unit tests (src/test): JUnit 4 and the coroutines test library (runTest, test dispatchers)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // instrumented tests (src/androidTest): AndroidX JUnit, Espresso (brings the test runner)
    // and the Compose test API (createComposeRule, onNodeWithText, performClick, ...)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // 'debugImplementation' only exists in the debug build variant:
    // tooling for previews, and an empty Activity that createComposeRule() starts the UI in
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
