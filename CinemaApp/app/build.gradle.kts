plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cinemaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cinemaapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mysql.connector.java)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp.urlconnection)
    testImplementation(libs.junit)
    implementation(libs.glide)
    implementation(libs.picasso)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.gson)
    annotationProcessor(libs.glide.compiler)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}