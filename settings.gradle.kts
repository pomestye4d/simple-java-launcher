include(":launcher")
include(":examples:headless-app")
include(":gradle-plugins:dist")
rootProject.name ="simple-java-launcher"
pluginManagement {
    repositories {
        maven{
            name="local-project"
            url = uri("local-maven-repository")
        }
        gradlePluginPortal()
    }
}