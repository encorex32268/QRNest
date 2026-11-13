plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.database)
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(projects.core.presentation)

            implementation(projects.feature.scan.domain)
            implementation(projects.feature.scan.data)
            implementation(projects.feature.scan.presentation)

            implementation(projects.feature.generate.domain)
            implementation(projects.feature.generate.data)
            implementation(projects.feature.generate.presentation)

            implementation(projects.feature.history.domain)
            implementation(projects.feature.history.data)
            implementation(projects.feature.history.presentation)

            implementation(projects.feature.preview.domain)
            implementation(projects.feature.preview.data)
            implementation(projects.feature.preview.presentation)

            implementation(libs.bundles.koin.common)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.jetbrains.lifecycle.compose)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
