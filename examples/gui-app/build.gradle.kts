plugins {
    java
    id("org.openjfx.javafxplugin" ) version "0.0.9"

}

repositories{
    mavenCentral()
}
java{
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

javafx {
    version = "16"
    modules = arrayListOf("javafx.controls", "javafx.fxml")
}

dependencies {

    // here starts JavaFX
//    implementation("org.openjfx:javafx:14")
//
//    implementation("org.openjfx:javafx-base:14")
//            implementation("org.openjfx:javafx-graphics:14")
//            implementation("org.openjfx:javafx-controls:14")
//            implementation("org.openjfx:javafx-fxml:14")
//            implementation("org.openjfx:javafx-swing:14")
//            implementation("org.openjfx:javafx-media:14")
//            implementation("org.openjfx:javafx-web:14")
    implementation(project(":launcher"))
}

configurations.create("dist"){
    extendsFrom(configurations.getByName("implementation"))
}

task("dist"){
    group = "build"
    dependsOn("build")
    doLast {
        file("build/dist").deleteRecursively()
        file("build/dist/lib").mkdirs()
        file("build/libs/gui-app.jar").copyTo(file("build/dist/lib/gui-app.jar"))
        configurations.getByName("dist").forEach {
            it.copyTo(file("build/dist/lib/${it.name}"))
        }
        file("config.yml").copyTo(file("build/dist/config.yml"))
        file("../../native/gui/cmake-build-debug/sjl").copyTo(file("build/dist/gui-app"))
        file("build/dist/gui-app").setExecutable(true)
    }
}