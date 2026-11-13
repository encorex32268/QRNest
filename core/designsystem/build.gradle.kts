plugins {
    alias(libs.plugins.convention.cmp.library)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.lihan.qrnest.core.designsystem.resources"
}
