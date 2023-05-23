plugins {
    java
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
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5+")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.vga.sjl.SjlBoot"
    }
}

tasks.test {
    useJUnitPlatform()
}