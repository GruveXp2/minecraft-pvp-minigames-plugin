import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    api("org.jetbrains:annotations:24.1.0")
    compileOnly("io.papermc.paper:paper-api:1.21.+")
}

val buildNumberFile = file("build-number.txt")
val buildNumber: Int = if (buildNumberFile.exists()) {
    val currentBuildNumber = buildNumberFile.readText().trim().toInt()
    buildNumberFile.writeText("${currentBuildNumber + 1}")
    currentBuildNumber + 1
} else {
    buildNumberFile.writeText("1")
    1
}

group = "gruvexp"
version = "1.4.3-$buildNumber"
description = "The plugin used on the BotBows minigames server"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register("incrementBuildNumber") {
    doLast {
        val currentBuildNumber = buildNumberFile.readText().trim().toInt()
        val newBuildNumber = currentBuildNumber + 1
        buildNumberFile.writeText(newBuildNumber.toString())
        println("Build number incremented to $newBuildNumber")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "gruvexp.bbminigames.Main"
    }
    archiveVersion.set("")
    archiveClassifier.set("")
}