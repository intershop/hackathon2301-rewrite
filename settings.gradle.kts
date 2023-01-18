pluginManagement {
    val DockerPluginVersion = "2.3.1"
    val ICMGradlePluginVersion = "5.6.1"
    
    plugins {
        id("com.intershop.gradle.icm.project") version ICMGradlePluginVersion
        id("com.intershop.icm.cartridge.product") version ICMGradlePluginVersion

        id("com.intershop.icm.crossproject") version ICMGradlePluginVersion

        id("com.intershop.gradle.escrow-plugin") version "1.0.3"

        id("com.intershop.gradle.icm.docker.customization") version DockerPluginVersion
        id("com.intershop.gradle.icm.docker.solrcloud") version DockerPluginVersion
        id("com.intershop.gradle.icm.docker.gebtest") version DockerPluginVersion

        id("com.intershop.gradle.cartridge-resourcelist") version "4.4.1"
        id("com.intershop.gradle.jaxb") version "5.2.1"
        id("com.intershop.gradle.isml") version "6.3.0"
        id("org.gradle.test-retry") version "1.3.1"

        id("com.intershop.gradle.version.gitflow") version "1.7.5"
        id("com.dorongold.task-tree") version "2.1.0"

        id("org.sonarqube") version "3.4.0.2513"
    }

    val repoUser: String by settings
    val repoPassword: String by settings

    repositories {
        /*
        maven {
            name = "PluginRepository"
            url = uri("https://pkgs.dev.azure.com/${adoOrganizationName}/${adoProjectName}/_packaging/icm-as-libraries-releases/maven/v1")
            credentials {
                username = repoUser
                password = repoPassword
            }
        }
        */
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

plugins {
    id("com.gradle.enterprise").version("3.5.1")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("selenium", "3.141.59")
            version("testscontainers", "1.16.0")
            version("groovy", "2.5.4")

            library("selenium-api", "org.seleniumhq.selenium", "selenium-api").versionRef("selenium")
            library("selenium-support", "org.seleniumhq.selenium", "selenium-support").versionRef("selenium")
            library("selenium-remote-driver", "org.seleniumhq.selenium", "selenium-remote-driver").versionRef("selenium")
            library("selenium-firefox-driver", "org.seleniumhq.selenium", "selenium-firefox-driver").versionRef("selenium")
            library("selenium-chrome-driver", "org.seleniumhq.selenium", "selenium-chrome-driver").versionRef("selenium")

            library("testscontainers-spock", "org.testcontainers", "spock").versionRef("testscontainers")
            library("testscontainers-core", "org.testcontainers", "testcontainers").versionRef("testscontainers")
            library("testscontainers-selenium", "org.testcontainers", "selenium").versionRef("testscontainers")

            library("groovy-core", "org.codehaus.groovy", "groovy").versionRef("groovy")
            library("groovy-http-builder", "org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")

            library("rest-spock-core", "org.spockframework:spock-core:1.3-groovy-2.5")
            library("rest-hamcrest", "org.hamcrest:hamcrest-library:1.3")

            library("geb-spock", "org.gebish:geb-spock:5.0")
            library("geb-slf4j", "org.slf4j:slf4j-simple:1.7.35")
            library("spock-core", "org.spockframework:spock-core:2.0-groovy-3.0")

            bundle("selenium", listOf("selenium-api", "selenium-support", "selenium-remote-driver", "selenium-firefox-driver", "selenium-chrome-driver"))
            bundle("testscontainers", listOf("testscontainers-spock", "testscontainers-core", "testscontainers-selenium"))
            bundle("geb", listOf("geb-spock", "spock-core", "geb-slf4j"))
            bundle("resttest", listOf("groovy-core", "groovy-http-builder", "rest-spock-core", "rest-hamcrest"))
        }
    }
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

// define root project name
rootProject.name = "hackathon2301-rewrite"

include("versions")
include("versions_test")
include("migration")
include("my_cartridge")
include("my_cartridge_test")
include("ft_production")
include("ft_test")
include("app_sf_responsive")
