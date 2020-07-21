plugins {
    kotlin("multiplatform") version "1.3.72"
    id("maven-publish")
}
repositories {
    mavenCentral()
}
group = ""
version = "0.0.1"

kotlin {
    jvm()
    js {
        browser {
        }
        nodejs {
        }
    }
    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64
    // macosX64("macos")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation('io.kotest:kotest-runner-junit5-jvm:4.1.2')
                implementation('io.kotest:kotest-assertions-core-jvm:4.1.2')
                implementation('io.kotest:kotest-property-jvm:4.1.2')
                implementation('io.kotest:kotest-runner-console-jvm:4.1.2')
                implementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        /*
        macosMain {
        }
        macosTest {
        }
        */

        tasks.named("jvmTest") {
            useJUnitPlatform()
        }
    }
}

