plugins {
    kotlin("jvm")
    application
}

group = "edu.kit.kastel.logic"
version = "1.0-SNAPSHOT"

application {
    mainClass = "edu.kit.kastel.vads.compiler.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}