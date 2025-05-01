// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false // Uncomment this to use Firebase
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false // Uncomment this to use Firebase
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}