import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.crylo.kidreels.ui"
        compileSdk = 37
        minSdk = 31
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    // Not shipped yet: compiling for desktop proves the UI has no Android imports, so a
    // desktop or iOS app later is a new shell, not a rewrite.
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
    }
}
