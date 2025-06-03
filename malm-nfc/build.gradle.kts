import com.github.gradle.node.npm.task.NpmTask

plugins {
    base
    id("com.github.node-gradle.node") version "7.0.1"
}

node {
    version.set("21.6.2")
    npmVersion = ""
    yarnVersion = ""

    download.set(true)
    workDir = file("${rootProject.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${rootProject.projectDir}/.gradle/npm")
    yarnWorkDir = file("${rootProject.projectDir}/.gradle/yarn")
}

tasks.register<NpmTask>("rebuild") {
    description = "Rebuilds"
    args.set(listOf("rebuild"))
}

// Task to install npm dependencies
tasks.npmInstall {
    description = "Install Node.js dependencies"
    dependsOn("rebuild")
}

// Task to build TypeScript code
tasks.register<NpmTask>("buildTypeScript") {
    description = "Build TypeScript code"
    args.set(listOf("run", "build"))
    dependsOn(tasks.npmInstall)
}

// Task to run the read-tag script
tasks.register<NpmTask>("readTag") {
    description = "Run the read-tag script"
    args.set(listOf("run", "read-tag"))
    dependsOn(tasks.npmInstall)
}

tasks.register<NpmTask>("writeTag") {
    description = "Run the write-tag script"
    args.set(listOf("run", "write-tag"))
    dependsOn(tasks.npmInstall)
}

// Task to run in development mode
tasks.register<NpmTask>("devMode") {
    description = "Run in development mode"
    args.set(listOf("run", "dev"))
    dependsOn(tasks.npmInstall)
}

// Make the standard Gradle build task depend on the TypeScript build
tasks.named("build") {
    dependsOn("buildTypeScript")
}

// Extend the clean task to remove node_modules and dist directories
tasks.named("clean") {
    doLast {
        delete("node_modules")
        delete("dist")
    }
}
