import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.jetbrains.compose")
  id("org.jetbrains.changelog")
  id("org.jetbrains.intellij.platform")
}

dependencies {
  implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.junit)

  intellijPlatform {
    intellijIdea("2025.3.5")
    testFramework(TestFrameworkType.Platform)

    // Add plugin dependencies for compilation here:
    bundledPlugin("com.intellij.java")
    bundledPlugin("org.jetbrains.kotlin")
    bundledPlugin("JavaScript")

    // Jewel (Compose for Desktop) ships bundled with the IntelliJ Platform since 251.2+.
    // Rely on the platform-provided Compose runtime instead of bundling our own.
    bundledModule("intellij.platform.jewel.foundation")
    bundledModule("intellij.platform.jewel.ui")
    bundledModule("intellij.platform.jewel.ideLafBridge")
    bundledModule("intellij.libraries.compose.foundation.desktop")
    bundledModule("intellij.libraries.skiko")
  }
}
