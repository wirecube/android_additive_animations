plugins {
    id("com.android.application")
}

android {
    namespace = "at.wirecube.additiveanimations.additiveanimationsdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "additive_animations.demo"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation(project(":additive_animations"))
    implementation("com.bartoszlipinski:viewpropertyobjectanimator:1.4.5")
    implementation("com.google.code.gson:gson:2.11.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

