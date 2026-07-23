import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "clickUp"

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.changelog") version "2.5.0"
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("org.jetbrains.intellij.platform.settings") version "2.18.1"
}
dependencyResolutionManagement {
  // Configure all projects' repositories
  repositories {
    google()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
      defaultRepositories()
    }
  }
}
