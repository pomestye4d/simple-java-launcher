plugins {
    java
    `maven-publish`
}
repositories{
    mavenCentral()
}
java{
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.vga.sjl.SjlBoot"
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing{
    publications{
        create<MavenPublication>("launcher") {
            groupId = "com.vga"
            artifactId = "sjl-launcher"
            version = "0.0.1"
            from(components["java"])
        }
    }
    println(project.file("../local-maven-repository").toURI())
    repositories {
        maven{
            name = "projectLocal"
            url = project.file("../local-maven-repository").toURI()
        }
    }
}