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

val isDebug = System.getenv("CONFIGURATION")?.equals("Debug", ignoreCase = true) == true ||
        gradle.startParameter.taskNames.any {
            it.contains("debug", ignoreCase = true) || it.contains("dev", ignoreCase = true)
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
// RevenueCat public SDK keys (Test Store / sandbox keys work too). Blank keys disable purchases.
val revenueCatAndroidApiKey = if(!isDebug)localProperties.getProperty("REVENUECAT_TEST_KEY")?.trim().orEmpty() else localProperties.getProperty("REVENUECAT_ANDROID_API_KEY")?.trim().orEmpty()
val revenueCatIosApiKey = if(isDebug)localProperties.getProperty("REVENUECAT_TEST_KEY")?.trim().orEmpty() else localProperties.getProperty("REVENUECAT_IOS_API_KEY")?.trim().orEmpty()
val vertexConfigDir = layout.buildDirectory.dir("generated/vertexConfig/commonMain/kotlin")

val generateVertexConfig by tasks.registering {
    val apiKey = vertexApiKey
    val model = vertexModel
    val revenueCatAndroidKey = revenueCatAndroidApiKey
    val revenueCatIosKey = revenueCatIosApiKey
    val outputDir = vertexConfigDir
    inputs.property("apiKey", apiKey)
    inputs.property("model", model)
    inputs.property("revenueCatAndroidKey", revenueCatAndroidKey)
    inputs.property("revenueCatIosKey", revenueCatIosKey)
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
        outputDir.get().file("org/techascent/aifinder/config/RevenueCatConfig.kt").asFile.writeText(
            """
            |package org.techascent.aifinder.config
            |
            |internal object RevenueCatConfig {
            |    const val ANDROID_API_KEY: String = ${revenueCatAndroidKey.quoted()}
            |    const val IOS_API_KEY: String = ${revenueCatIosKey.quoted()}
            |}
            |""".trimMargin()
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "AiFinder"
            isStatic = true
            binaryOption("bundleId", "org.techascent.aifinder.shared")
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        all {
            if (name.startsWith("ios")) languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
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
            implementation(libs.navigation.aifinder)
            implementation(libs.moko.aifinder.core)
            implementation(libs.moko.aifinder.compose)
            implementation(libs.moko.aifinder.camera)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.ktor.client.content.negotiation)
            implementation(libs.ktor.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.revenuecat.purchases.core)
            implementation(libs.revenuecat.purchases.ui)
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
            implementation(libs.core)
            implementation(libs.androidx.activity.compose)
            implementation(libs.bundles.camera)
            implementation(libs.ktor.client.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.database)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.functions)
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
        versionCode = 4
        versionName = "1.4"
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$firebaseDatabaseUrl\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Lets JVM unit tests run code that logs through android.util.Log.
    testOptions.unitTests.isReturnDefaultValues = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
