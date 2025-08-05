plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
}

android {
    namespace = "org.app.glimpse"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.app.glimpse"
        minSdk = 31
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.maps.mobile)
    implementation(libs.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    // Ядро клиента Ktor
    implementation(libs.ktor.client.core) // Проверьте наличие последней стабильной версии
    // Движок клиента Ktor (выберите один - CIO часто хорош для Android)
    implementation(libs.ktor.client.cio) // Или 'ktor-client-okhttp', 'ktor-client-android'
    // Ktor Content Negotiation (для сериализации/десериализации JSON)
    implementation(libs.ktor.client.content.negotiation)
    // Kotlinx Serialization (для преобразования JSON)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.datetime) // Актуальная версия kotlinx-datetime
    // Опционально: Логирование (очень полезно для отладки сетевых запросов)
    implementation("io.ktor:ktor-client-logging:3.2.3")
}