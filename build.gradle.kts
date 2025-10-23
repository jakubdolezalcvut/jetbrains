plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
}

group = "com.excel"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":calculator:impl"))
    implementation(project(":ui"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    implementation(libs.kotlin.logging)
    implementation(libs.slf4j)
    implementation(libs.logback)
}
