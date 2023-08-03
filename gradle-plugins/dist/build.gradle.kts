plugins {
    kotlin("jvm")  version "1.8.21"
    id("com.gradle.plugin-publish") version "1.1.0"
}
repositories{
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

kotlin {
    jvmToolchain(8)
}

group = "com.vga.sjl"
version = "0.0.1"

gradlePlugin {
    website.set("http://gridnine.com")
    vcsUrl.set("https://github.com/pomestye4d/simple-java-launcher")
    plugins {
        create("dist") {
            id = "com.vga.sjl.dist"
            displayName = "SJL Distribution plugin"
            description = "Create distribution for SJL projects"
            tags.set(listOf("java", "distribution"))
            implementationClass = "com.vga.sjl.gradle.dist.SjlDistPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "projectLocal"
            url = uri("../../local-maven-repository")
        }
    }
}