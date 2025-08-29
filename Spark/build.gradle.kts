import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.21"
}

group = "org.adprasad"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_23)
        freeCompilerArgs.set(listOf("-Xjvm-default=all"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("me.friwi:jcefmaven:135.0.20")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}