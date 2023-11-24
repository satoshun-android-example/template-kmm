/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.satoshun.example

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Configure Compose-specific options
 */
internal fun Project.configureMultiplatformCompose(
  extension: ComposeExtension,
) {
  extension.apply {
    val jbVersion = libs.findVersion("jbCompose").get()
    kotlinCompilerPlugin.set(jbVersion.toString())
    val kotlinVersion = libs.findVersion("kotlin").get().toString()
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=$kotlinVersion")
  }
}


/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: CommonExtension<*, *, *, *, *>,
) {
  commonExtension.apply {
    buildFeatures {
      compose = true
    }

    composeOptions {
      kotlinCompilerExtensionVersion = libs.findVersion("composeCompiler").get().toString()
    }

    dependencies {
      val bom = libs.findLibrary("compose-bom").get()
      add("implementation", platform(bom))
      add("androidTestImplementation", platform(bom))
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters()
    }
  }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
  val metricParameters = mutableListOf<String>()
  val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
  val enableMetrics = (enableMetricsProvider.orNull == "true")
  if (enableMetrics) {
    val metricsFolder = File(project.buildDir, "compose-metrics")
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
    )
  }

  val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
  val enableReports = (enableReportsProvider.orNull == "true")
  if (enableReports) {
    val reportsFolder = File(project.buildDir, "compose-reports")
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
    )
  }
  return metricParameters.toList()
}
