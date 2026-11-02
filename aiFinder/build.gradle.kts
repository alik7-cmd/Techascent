import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

// Vertex AI settings come from the git-ignored local.properties so the key is never committed.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val vertexApiKey = localProperties.getProperty("VERTEX_API_KEY").orEmpty()
val vertexModel = localProperties.getProperty("VERTEX_MODEL")?.takeIf { it.isNotBlank() } ?: "gemini-2.5-flash"
// Override with FIREBASE_DATABASE_URL in local.properties to point at another instance.
val firebaseDatabaseUrl = localProperties.getProperty("FIREBASE_DATABASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
    ?: ""
// Firebase Storage bucket for scan photos, e.g. gs://<project>.firebasestorage.app.
val firebaseStorageBucket = localProperties.getProperty("FIREBASE_STORAGE_BUCKET")?.trim().orEmpty()
val vertexConfigDir = layout.buildDirectory.dir("generated/vertexConfig/commonMain/kotlin")

val generateVertexConfig by tasks.registering {
    val apiKey = vertexApiKey
    val model = vertexModel
    val outputDir = vertexConfigDir
    inputs.property("apiKey", apiKey)
    inputs.property("model", model)
    outputs.dir(outputDir)
    doLast {
        fun String.quoted() = "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$") + "\""
        val file = outputDir.get().file("org/techascent/aifinder/config/VertexAiConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package org.techascent.aifinder.config
            |
            |internal object VertexAiConfig {
            |    const val API_KEY: String = ${apiKey.quoted()}
            |    const val MODEL: String = ${model.quoted()}
            |}
            |""".trimMargin()
        )
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(projects.composa)
            implementation(libs.androidx.lifecycle)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.ktor.client.content.negotiation)
            implementation(libs.ktor.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonMain {
            kotlin.srcDir(generateVertexConfig)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.sqlite.framework)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.bundles.camera)
            implementation(libs.ktor.client.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.database)
            implementation(libs.firebase.storage)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
            implementation(libs.kotlinx.coroutines.play.services)
        }

        iosMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(projects.composa)
            implementation(libs.androidx.lifecycle)
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "org.techascent.aifinder"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.techascent.aifinder"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$firebaseDatabaseUrl\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
