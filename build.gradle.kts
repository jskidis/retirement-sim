plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"
val apacheCsvVersion = "1.10.0"
val kotestRunnerVersion = "5.8.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-csv:$apacheCsvVersion")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestRunnerVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}