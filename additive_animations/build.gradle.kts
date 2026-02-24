import java.util.Properties

/*
 *  Copyright 2021 David Ganster
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "at.wirecube.additive_animations"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "at.wirecube"
                artifactId = "additive_animations"
                version = "1.10.1"

                pom {
                    name.set("Android Additive Animations")
                    description.set("Additive animations for Android, along with many convenience methods for clean animation code.")
                    url.set("https://github.com/davidganster/android_additive_animations")

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("davidganster")
                            name.set("David Ganster")
                        }
                    }

                    scm {
                        connection.set("scm:git@github.com/davidganster/android_additive_animations")
                        developerConnection.set("scm:git@github.com/davidganster/android_additive_animations")
                        url.set("https://github.com/davidganster/android_additive_animations")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "WirecubeNexus"
                url = uri("https://nexus.wirecube.at/repository/shopreme-core-android/")
                // load credentials from local.properties, which is not committed to version control, to avoid exposing credentials:
                val localProperties = project.rootProject.file("local.properties")
                // if local properties file doesn't exist, fail. otherise, use the nexus.username and nexus.password properties:
                if (!localProperties.exists()) {
                    return@maven
                } else {
                    val properties = Properties()
                    properties.load(localProperties.inputStream())
                    val username = properties.getProperty("nexus.username")
                    val password = properties.getProperty("nexus.password")
                    if (username != null && password != null) {
                        credentials {
                            this.username = username
                            this.password = password
                        }
                    }
                }
            }
        }
    }
}

