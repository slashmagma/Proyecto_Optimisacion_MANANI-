import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

// ===== API keys (guardadas en local.properties, NO se suben a git) =====
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) {
        archivo.inputStream().use { load(it) }
    }
}

fun propiedadLocal(nombre: String): String =
    (localProperties.getProperty(nombre) ?: "").replace("\"", "\\\"")

android {
    namespace = "com.Fenix.manami"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.Fenix.manami"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Claves de las APIs
        buildConfigField("String", "GEMINI_API_KEY", "\"${propiedadLocal("GEMINI_API_KEY")}\"")
        buildConfigField("String", "GROQ_API_KEY", "\"${propiedadLocal("GROQ_API_KEY")}\"")
        buildConfigField("String", "RESEND_API_KEY", "\"${propiedadLocal("RESEND_API_KEY")}\"")
        buildConfigField("String", "RESEND_FROM", "\"${propiedadLocal("RESEND_FROM")}\"")
        buildConfigField("String", "CORREO_ALERTAS", "\"${propiedadLocal("CORREO_ALERTAS")}\"")
        buildConfigField("String", "CORREO_ALERTAS2", "\"${propiedadLocal("CORREO_ALERTAS2")}\"")
        // Segunda cuenta de Resend (permite enviar también a Marcia)
        buildConfigField("String", "RESEND_API_KEY2", "\"${propiedadLocal("RESEND_API_KEY2")}\"")
        buildConfigField("String", "RESEND_FROM2", "\"${propiedadLocal("RESEND_FROM2")}\"")
        // Brevo: proveedor de correo principal (si no hay clave, se usa Resend)
        buildConfigField("String", "BREVO_API_KEY", "\"${propiedadLocal("BREVO_API_KEY")}\"")
        buildConfigField("String", "CORREO_DESDE", "\"${propiedadLocal("CORREO_DESDE")}\"")
        buildConfigField("String", "CORREO_DESDE_NOMBRE", "\"${propiedadLocal("CORREO_DESDE_NOMBRE")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Supabase SDK
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:supabase-kt")
    // Motor HTTP Ktor necesario para Android
    implementation("io.ktor:ktor-client-android:3.0.0")
    // Ktor ContentNegotiation para llamadas a APIs externas (Gemini / Resend)
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")

    // Serialización JSON para mapear datos de Supabase a clases Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    // Login
    implementation("io.github.jan-tennert.supabase:auth-kt")
    // Navegación para Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // Selector de archivos (abrir copia de seguridad desde el dispositivo)
    implementation("androidx.activity:activity-compose:1.8.0")
    // Jetpack Compose BOM y Material 3
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    // Cargar imágenes desde Supabase Storage (fotos permanentes del inventario)
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Copia de seguridad automática: WorkManager corre el trabajo aunque la app esté cerrada
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}