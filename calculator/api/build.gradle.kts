plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
}

group = "com.excel"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions.core)
}
