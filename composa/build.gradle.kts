
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.libs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
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
    jvm()

    sourceSets {
        val commonMain by getting {
            resources.srcDirs("src/commonMain/composeResources")
            resources.srcDirs("src/commonMain/resources")
            dependencies {
                // Kotlinx Coroutines
                //implementation(libs.kotlinx.coroutines.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                // uiToolingPreview must not ship in release — added to androidMain debug below
                implementation(libs.androidx.lifecycle)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                // Ktor client dependency required for Coil
                implementation(libs.ktor.client.android)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // Ktor client dependency required for iOS
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

android {
    namespace = "org.techascent.composa"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

// debugImplementation must live in the top-level dependencies block,
// not inside kotlin { sourceSets { ... } } which only accepts KMP scopes.
dependencies {
    debugImplementation(compose.components.uiToolingPreview)
    debugImplementation(compose.uiTooling)
}

