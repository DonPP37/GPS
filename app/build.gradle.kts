plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gpszonasestudiantesapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.gpszonasestudiantesapp"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.constraintlayout)

    // Dependencia solicitada en la guía del laboratorio GPS
    implementation("com.google.android.gms:play-services-location:17.1.0")

    // Persistencia Room para registrar estudiantes y agrupar zonas
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
}
