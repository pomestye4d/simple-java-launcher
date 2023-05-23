plugins {
    java
}
buildscript {
    dependencies{
        classpath(files(File(projectDir.parentFile.parentFile, "local-artifacts/sjl-gradle.jar")))
        classpath("org.snakeyaml:snakeyaml-engine:2.5")
        classpath("org.apache.httpcomponents.client5:httpclient5:5.1.3")
        classpath("com.jcraft:jsch:0.1.55")
    }
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
        archiveFileName.set(file("build/webapps/webapp.war").absolutePath)
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
        file("build/webapps/webapp.war").copyTo(file("build/dist/lib/webapp.war"))
        file("../../native/unix-headless/cmake-build-debug/sjl").copyTo(file("build/dist/headless-app"))
        file("build/dist/headless-app").setExecutable(true)
    }
}

task("deploy", com.vga.sjl.gradle.UpdateTask::class){
    group = "build"
    localLibsDirectory = file("build/dist/lib")
    port = 8082
    host = "localhost"
//    ssh {
//        sshHost = "localhost"
//        sshPort = 22
//        login = "login"
//        password = "password"
//        remotePort = 8081
//        privateKey = file("path_to_private_key_file")
//    }
}