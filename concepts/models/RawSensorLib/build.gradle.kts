// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

/*
 * New shared module ':models-RawSensorLib', included automatically by
 * includeConcepts("models") in settings.gradle.kts (every subfolder with a build file).
 * It lives here and not in concepts/libs on purpose: only the two RawSensor apps use it, and
 * concepts/libs is for course-wide helpers like UiTools. Keep things close to where they are used.
 *
 * It holds the data layer that RawSensorMVVM and RawSensorMVI had as copy & paste before.
 * Now both apps use the very same repository, so the only difference between them is the
 * presentation layer - the pattern we actually want to compare.
 *
 * No Compose plugin here: a data layer knows nothing about the UI.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.fhac.android.conventions)
}

android {
    namespace = "de.fh_aachen.android.raw_sensor"
}

dependencies {
    // 'api' instead of 'implementation': our public API exposes StateFlow (SensorFlow.data),
    // so every module using this lib needs the coroutine types on its compile classpath, too.
    // '-android', not '-core': it is the Android flavour (Dispatchers.Main) and brings core along.
    // A direct dependency on the multiplatform root artifact '-core' is the likely trigger of an
    // Android Studio sync crash (NPE in ModelCacheV2Impl.toFlatLibraryList: an edge without a source).
    api(libs.kotlinx.coroutines.android)
}
