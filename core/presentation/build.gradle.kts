plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(libs.kotlinx.datetime)
            implementation(libs.bundles.koin.common)
            implementation(libs.jetbrains.lifecycle.compose)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
