plugins {
    java
}

repositories{
    mavenCentral()
}
dependencies{
    implementation(gradleApi())
    implementation("org.snakeyaml:snakeyaml-engine:2.5")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1.3")
    implementation("com.jcraft:jsch:0.1.55")

}

java{
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

val jarArchiveName = "sjl-gradle"

tasks.withType<Jar>{
    archiveBaseName.set(jarArchiveName)
}

task("updateLocalGradlePlugins"){
    dependsOn("build")
    group = "other"
    doLast{
        val gradleDir = File(projectDir.parentFile, "gradle")
        project.file("build/libs/${jarArchiveName}.jar").copyTo(File(gradleDir, "${jarArchiveName}.jar"), true)
    }
}

