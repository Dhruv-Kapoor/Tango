// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    tasks.register<Copy>("copyBuildFiles") {
        from(layout.buildDirectory.dir("outputs/apk/debug")) {
            include("*.apk")
        }
        rename {
            "Tango.apk"
        }
        into(layout.projectDirectory.dir("../builds"))
    }
}
