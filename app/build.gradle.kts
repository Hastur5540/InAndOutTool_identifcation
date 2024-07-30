plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.inandouttool_identification"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.inandouttool_identification"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // CameraX core library
    implementation("androidx.camera:camera-core:1.1.0")

    // Camera2 implementation
    implementation("androidx.camera:camera-camera2:1.1.0")

    // Lifecycle library for CameraX
    implementation("androidx.camera:camera-lifecycle:1.1.0")

    // Optional - CameraX View class
    implementation("androidx.camera:camera-view:1.0.0")

    // Optional - CameraX Extensions library (for additional features like Night Mode, HDR, etc.)
    implementation("androidx.camera:camera-extensions:1.0.0")


    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.3")
}