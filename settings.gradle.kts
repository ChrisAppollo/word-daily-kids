pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 腾讯镜像加速
        maven { url = uri("https://mirrors.cloud.tencent.com/maven/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "word-daily-kids"
include(":app")