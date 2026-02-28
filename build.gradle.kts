plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register("prePushChecks") {
    group = "verification"
    description = "Runs static analysis checks before git push."
    dependsOn(
        ":app:detekt",
        ":data_expenses:detekt",
        ":domain_expenses:detekt",
        ":feature_expenses:detekt",
        ":app:lintDebug",
        ":data_expenses:lintDebug",
        ":domain_expenses:lintDebug",
        ":feature_expenses:lintDebug"
    )
}
