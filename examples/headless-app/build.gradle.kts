import com.vga.sjl.gradle.dist.DistributionArchiveType
import com.vga.sjl.gradle.dist.dist
import org.gradle.api.plugins.internal.JavaPluginHelper

plugins {
    java
    id("com.vga.sjl.dist") version "0.0.1"
}
repositories{
    mavenCentral()
    maven {
        name = "project-local"
        url = project.file("../../local-maven-repository").toURI()
    }
}
java{
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.0.23")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.0.23")
    implementation("com.vga.sjl:launcher:0.0.1")
}

tasks.create("makeWar", War::class.java) {
    group = "sjl"
    from("webapp")
    archiveFileName.set("webapp.war")
    destinationDirectory.set(file("build/webapps"))
}

dist {
    common {
        appName = "sjl-headless"
        dependsOnTasks = arrayListOf("jar","makeWar")
        useAmazonJreDownloadUrlResolver(8)
        assets("lib"){
            from("build/webapps")
        }
    }
    linux64Directory("sjl-headless-linux-dir"){
        config("config/config.yml", "config.yml")
    }
    linux64Archive("sjl-headless-linux-archive"){
        config("config/config.yml", "config.yml")
        archiveType = DistributionArchiveType.TARGZ
    }
}
