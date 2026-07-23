import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.jetbrains.compose")
  id("org.jetbrains.changelog")
  id("org.jetbrains.intellij.platform")
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
kotlin {
  jvmToolchain(25)
}

intellijPlatform {
  // The platform's bytecode instrumenter can't yet read Java 25 class files (fails with a
  // cryptic "1 >= 1"). We don't rely on form/@NotNull instrumentation, so disable it.
  instrumentCode = false
}

dependencies {
  implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.junit)

  intellijPlatform {
    intellijIdea("2026.2")
    testFramework(TestFrameworkType.Platform)

    // Add plugin dependencies for compilation here:

    // Jewel (Compose for Desktop) ships bundled with the IntelliJ Platform since 251.2+.
    // Rely on the platform-provided Compose runtime instead of bundling our own.
    bundledModule("intellij.platform.jewel.foundation")
    bundledModule("intellij.platform.jewel.ui")
    bundledModule("intellij.platform.jewel.ideLafBridge")
    bundledModule("intellij.libraries.compose.runtime.desktop")
    bundledModule("intellij.libraries.compose.foundation.desktop")
    bundledModule("intellij.libraries.skiko")
  }
}
