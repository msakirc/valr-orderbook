val kotlin_version: String by project
val logback_version: String by project

val vertxVersion = "4.5.10"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.sakircimen.orderbook.Application"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "com.sakircimen.orderbook"
version = "0.0.1"

repositories {
    mavenCentral()
}

application {
    mainClass.set(launcherClassName)
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-auth-properties")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

}

tasks.withType<JavaExec> {
    args = listOf("run", mainVerticleName, "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
