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
    implementation("ch.qos.logback:logback-classic:1.4.3")
    implementation("org.slf4j:slf4j-api:2.0.1")
    implementation("org.slf4j:jul-to-slf4j:2.0.1")
    implementation(project(":launcher"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5+")
}

configurations.create("dist"){
    extendsFrom(configurations.getByName("implementation"))
}

task("createWebApp", Jar::class){
    dependsOn("build")
    from("webapp"){
        archiveFileName.set(file("build/webapps/webapp.war").absolutePath)
    }
}
task("dist"){
    group = "build"
    dependsOn("build", "createWebApp")
    doLast {
        file("build/dist").deleteRecursively()
        file("build/dist/lib").mkdirs()
        file("build/libs/updater.jar").copyTo(file("build/dist/lib/updater"))
        configurations.getByName("dist").forEach {
            it.copyTo(file("build/dist/lib/${it.name}"))
        }
        file("config.yml").copyTo(file("build/dist/config.yml"))
        file("build/webapps/webapp.war").copyTo(file("build/dist/lib/webapp.war"))
        file("../native/unix-headless/cmake-build-debug/sjl").copyTo(file("build/dist/updater"))
        file("build/dist/updater").setExecutable(true)
    }
}

tasks.test {
    useJUnitPlatform()
}