include(":launcher")
include(":examples:headless-app")
include(":gradle-plugins:dist")
rootProject.name ="simple-java-launcher"
pluginManagement {
    repositories {
        maven{
            name="local-project"
            url = uri("/home/avramenko/projects/own/simple-java-launcher/local-maven-repository")
        }
        gradlePluginPortal()
    }
}