plugins {
    java
}

repositories {
    mavenCentral()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.openjfx:javafx-base:20:linux")
    implementation("org.openjfx:javafx-controls:20:linux")
    implementation("org.openjfx:javafx-fxml:20:linux")
    implementation("org.openjfx:javafx-graphics:20:linux")

    implementation(project(":launcher"))
}

configurations.create("dist") {
    extendsFrom(configurations.getByName("implementation"))
}

