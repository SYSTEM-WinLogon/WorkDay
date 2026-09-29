import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinCompose)
    `maven-publish`
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties: Properties? = if (keystorePropertiesFile.exists()) {
    Properties().apply {
        keystorePropertiesFile.inputStream().use { stream -> load(stream) }
    }
} else {
    null
}

android {
    namespace = "daka.work.day"
    compileSdk = 37

    defaultConfig {
        applicationId = "daka.work.day"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val hasKeystoreConfig = keystoreProperties != null &&
            keystoreProperties.containsKey("storeFile") &&
            keystoreProperties.containsKey("storePassword") &&
            keystoreProperties.containsKey("keyAlias") &&
            keystoreProperties.containsKey("keyPassword")

        if (hasKeystoreConfig) {
            create("release") {
                storeFile = file(keystoreProperties!!.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystoreProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            groupId = "daka.work.day"
            artifactId = "workday"
            version = "1.0.0"

            artifact(layout.projectDirectory.file("build/outputs/apk/release/app-release.apk"))
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(
                "https://maven.pkg.github.com/${System.getenv("GITHUB_ACTOR") ?: "SYSTEM-WinLogon"}/WorkDay"
            )
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "SYSTEM-WinLogon"
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
