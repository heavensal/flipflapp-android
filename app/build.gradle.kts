import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

fun escapeForBuildConfig(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

val apiBaseUrl =
    project.findProperty("flipflapp.apiBaseUrl") as String?
        ?: localProperties.getProperty("flipflapp.apiBaseUrl")
        ?: "http://10.0.2.2:3000"

val googleMapsApiKey =
    project.findProperty("googleMaps.apiKey") as String?
        ?: localProperties.getProperty("googleMaps.apiKey")
        ?: ""

val googleServicesFile = file("google-services.json")
val hasGoogleServices = googleServicesFile.exists()

fun propOrLocal(name: String): String? =
    (project.findProperty(name) as String?)
        ?: System.getenv(name)
        ?: localProperties.getProperty(name)
        ?: localProperties.getProperty(name.lowercase())

val releaseKeystorePath = propOrLocal("KEYSTORE_FILE")
val releaseStorePassword = propOrLocal("KEYSTORE_PASSWORD")
val releaseKeyAlias = propOrLocal("KEY_ALIAS")
val releaseKeyPassword = propOrLocal("KEY_PASSWORD")
val hasReleaseSigning =
    !releaseKeystorePath.isNullOrBlank() &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

val releaseVersionCode =
    (project.findProperty("versionCode") as String?)?.toIntOrNull()
        ?: System.getenv("VERSION_CODE")?.toIntOrNull()
        ?: 1
val releaseVersionName =
    (project.findProperty("versionName") as String?)
        ?: System.getenv("VERSION_NAME")
        ?: "1.0.0"

android {
    namespace = "fr.flipflapp.android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "fr.flipflapp.android"
        minSdk = 26
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${escapeForBuildConfig(apiBaseUrl)}\"",
        )
        buildConfigField(
            "String",
            "GOOGLE_MAPS_API_KEY",
            "\"${escapeForBuildConfig(googleMapsApiKey)}\"",
        )
        buildConfigField("boolean", "PUSH_ENABLED", "$hasGoogleServices")
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                // Absolute paths (CI / home directory) or project-relative.
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Embed native debug symbols in the AAB for Play Console crash/ANR symbolication
            // (Maps / Places / Firebase ship .so libraries).
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_BASE_URL", "\"https://flipflapp.fr\"")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.places)
    implementation(libs.coil.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    debugImplementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

if (hasGoogleServices) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn(
        "google-services.json missing — FCM push disabled. " +
            "Copy app/google-services.json.example → app/google-services.json from Firebase Console.",
    )
}
