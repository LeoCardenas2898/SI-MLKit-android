plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    //kotlin("android.extensions")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")
    defaultConfig {
        applicationId = "com.untels.intelligent.systems"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.viewBinding = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    aaptOptions.noCompress("tflite")

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.21")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.preference:preference-ktx:1.1.1")
    //UI
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.google.android.material:material:1.2.1")
    //CameraX
    implementation("androidx.camera:camera-core:1.0.0-rc02")
    implementation("androidx.camera:camera-camera2:1.0.0-rc02")
    //MLKit
    implementation("com.google.mlkit:object-detection-custom:16.3.1")
    implementation("com.google.guava:guava:30.1-android")
    implementation("com.google.mlkit:translate:16.1.1")
    //ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
    //Unit Test
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}