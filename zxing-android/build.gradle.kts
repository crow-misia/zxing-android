import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.kotlin.android)
    id("signing")
    id("maven-publish")
}

object Maven {
    const val groupId = "io.github.crow-misia.zxing-android"
    const val artifactId = "zxing-android"
    const val name = "zxing-android"
    const val desc = "ZXing for Android"
    const val version = "0.6.0"
    const val siteUrl = "https://github.com/crow-misia/zxing-android"
    const val gitUrl = "https://github.com/crow-misia/zxing-android.git"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
}

group = Maven.groupId
version = Maven.version

android {
    namespace = "io.github.crow_misia.zxing_android"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        textReport = true
        checkDependencies = true
        baseline = file("lint-baseline.xml")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        javaParameters = true
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.androidx.annotation)

    compileOnly(libs.androidx.camera.core)

    api(libs.com.google.zxing.core)

    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.truth)
    androidTestImplementation(libs.truth)
}


val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            afterEvaluate {
                from(components.named("release").get())
            }

            groupId = Maven.groupId
            artifactId = Maven.artifactId

            println("""
                |Creating maven publication
                |    Group: $groupId
                |    Artifact: $artifactId
                |    Version: $version
            """.trimMargin())

            artifact(dokkaJavadocJar)

            pom {
                name = Maven.name
                description = Maven.desc
                url = Maven.siteUrl

                scm {
                    val scmUrl = "scm:git:${Maven.gitUrl}"
                    connection = scmUrl
                    developerConnection = scmUrl
                    url = Maven.gitUrl
                    tag = "HEAD"
                }

                developers {
                    developer {
                        id = "crow-misia"
                        name = "Zenichi Amano"
                        email = "crow.misia@gmail.com"
                        roles = listOf("Project-Administrator", "Developer")
                        timezone = "+9"
                    }
                }

                licenses {
                    license {
                        name = Maven.licenseName
                        url = Maven.licenseUrl
                        distribution = Maven.licenseDist
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = providers.gradleProperty("sona.user").orElse(providers.environmentVariable("SONA_USER")).orNull
                password = providers.gradleProperty("sona.password").orElse(providers.environmentVariable("SONA_PASSWORD")).orNull
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
    config.from(rootDir.resolve("config/detekt.yml"))
}
