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
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.0.23")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.0.23")
    implementation(project(":launcher"))
}

configurations.create("dist"){
    extendsFrom(configurations.getByName("implementation"))
}
task("createWebApp", Jar::class){
    dependsOn("build")
    from("webapp"){
        archiveFileName.set(file("build/build/libs/webapp.war").absolutePath)
    }
}
task("dist"){
    group = "build"
    dependsOn("build", "createWebApp")
    doLast {
        file("build/dist").deleteRecursively()
        file("build/dist/lib").mkdirs()
        file("build/libs/headless-app.jar").copyTo(file("build/dist/lib/headless-app.jar"))
        configurations.getByName("dist").forEach {
            it.copyTo(file("build/dist/lib/${it.name}"))
        }
        file("config.yml").copyTo(file("build/dist/config.yml"))
    }
}