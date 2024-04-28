plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.skidis.retirement-sim"
version = "1.0-SNAPSHOT"
val apacheCsvVersion = "1.10.0"
val kotestRunnerVersion = "5.8.0"
val coroutinesCoreVersion = "1.8.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-csv:$apacheCsvVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesCoreVersion")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestRunnerVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}