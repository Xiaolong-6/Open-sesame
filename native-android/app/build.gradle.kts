plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xl6.opensesame"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xl6.opensesame"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-native-lite"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")

    // QR scanning. Remove these four dependencies if you later choose a no-camera ultra-lite build.
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
}
