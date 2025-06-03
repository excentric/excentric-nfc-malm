// Simple Gradle build file for TypeScript project

// Apply the base plugin and node plugin
plugins {
    base
    id("com.github.node-gradle.node") version "7.0.1"
}

// Configure the node plugin
node {
    // Set the version of Node.js to use
    version.set("20.0.0")
    // Download node using the plugin
    download.set(true)
    // Set the working directory for node tasks
    workDir.set(file("${project.buildDir}/nodejs"))
    // Set the directory for npm packages
    npmWorkDir.set(file("${project.buildDir}/npm"))
    // Set the directory for yarn packages (if used)
    yarnWorkDir.set(file("${project.buildDir}/yarn"))
}

// Task to install npm dependencies
tasks.npmInstall {
    description = "Install Node.js dependencies"
}

// Task to build TypeScript code
tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildTypeScript") {
    description = "Build TypeScript code"
    args.set(listOf("run", "build"))
    dependsOn(tasks.npmInstall)
}

// Task to run the application
tasks.register<com.github.gradle.node.npm.task.NpmTask>("runApp") {
    description = "Run the application"
    args.set(listOf("run", "start"))
    dependsOn("buildTypeScript")
}

// Task to run the read-tag script
tasks.register<com.github.gradle.node.npm.task.NpmTask>("readTag") {
    description = "Run the read-tag script"
    args.set(listOf("run", "read-tag"))
    dependsOn(tasks.npmInstall)
}

// Task to run in development mode
tasks.register<com.github.gradle.node.npm.task.NpmTask>("devMode") {
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
