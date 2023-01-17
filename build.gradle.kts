import com.intershop.gradle.icm.docker.extension.geb.GebConfiguration
import com.intershop.gradle.icm.docker.tasks.geb.GebTest

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.lesscss:lesscss:1.7.0.1.1")
    }
}

plugins {
    // IDE plugin
    idea
    // Gradle base plugin
    base
    // Gradle project report plugin
    `project-report`
    // Gradle dashboard plugin
    `build-dashboard`

    id("org.gradle.test-retry")

    id("com.intershop.gradle.version.gitflow")

    id("com.intershop.gradle.icm.project")

    id("com.intershop.gradle.icm.docker.customization")
    id("com.intershop.gradle.icm.docker.solrcloud")

    id("com.dorongold.task-tree")
    id("com.github.ben-manes.versions") version "0.42.0"
}

gitflowVersion {
    versionType = "three"
    defaultVersion = "1.0.0"
}

description = "<@description@>"
group = "<@group@>"
version = gitflowVersion.version

// version configuration
val icmVersion: String by project
val solrVersion: String by project
val headlessVersion: String by project

val icmCustomizationBaseVersion: String by project
val icmWAVersion: String by project
val icmWAAgentVersion: String by project

// Docker registry configuration
val icmDockerRegistry: String by project
val dockerRegistry: String by project

// used for repo access
val adoOrganizationName: String by project
val adoProjectName: String by project

// used for publishing
val repoUser: String by project
val repoPassword: String by project

repositories {
    mavenLocal()
    mavenCentral()

    // can be removed for a customization configuration
    maven {
        name = "ICM-AS"
        url = uri("https://pkgs.dev.azure.com/${adoOrganizationName}/${adoProjectName}/_packaging/icm-as-releases/maven/v1")
        credentials {
            username = repoUser
            password = repoPassword
        }
        mavenContent {
            releasesOnly()
        }
    }
    // can be removed for a customization configuration
    maven {
        name = "SOLRCLOUD"
        url = uri("https://pkgs.dev.azure.com/${adoOrganizationName}/${adoProjectName}/_packaging/icm-solrcloud-releases/maven/v1")
        credentials {
            username = repoUser
            password = repoPassword
        }
        mavenContent {
            releasesOnly()
        }
    }
}

intershop {
    projectInfo {
        productID.set("<@productID@>")
        productName.set("<@productName@>")
        copyrightOwner.set("<@copyrightOwner@>")
        copyrightFrom.set("<@copyrightFrom@>")
        organization.set("<@organization@>")
    }

    projectConfig {
        base {
            dependency.set("com.intershop.icm:icm-as:${icmVersion}")
            platforms.add("com.intershop.icm:versions:${icmVersion}")

            image.set("${icmDockerRegistry}/icm-as:${icmVersion}")
            testImage.set("${icmDockerRegistry}/icm-as-test:${icmVersion}")
        }

        modules {
        }

        serverDirConfig {
            base {
                target.set("system-conf")
            }
        }
    }
}

intershop_docker {
    images {
        icmcustomizationbase.set("${icmDockerRegistry}/icm-as-customization-base:${icmCustomizationBaseVersion}")

        webadapter.set("${icmDockerRegistry}/icm-webadapter:${icmWAVersion}")
        webadapteragent.set("${icmDockerRegistry}/icm-webadapteragent:${icmWAAgentVersion}")

        mssqldb.set("${icmDockerRegistry}/mssql-intershop:2019-1.0")

        solr.set("solr:8")
        zookeeper.set("zookeeper:3.8")

        mailsrv.set("mailhog/mailhog:latest")
    }

    ishUnitTests {
        register("myTest") {
            cartridge.set("my_cartridge_test")
            testSuite.set("tests.suite.MyCartridgeSuite")
        }
    }

    imageBuild {
        license.set("Intershop Communications AG")
        maintainer.set("Intershop Communications AG 'www.intershop.de'")
        baseDescription.set("Intershop Commerce Management")

        baseImageName.set("${dockerRegistry}/icm-as-customization-${project.name}")
        images {
            mainImage {
                dockerBuildDir.set("main")
                enabled.set(true)
            }
            testImage {
                dockerBuildDir.set("test")
                enabled.set(false)
            }
        }
    }

    developmentConfig {
        appserverAsContainer = true
        cartridgeList.set(setOf("ft_production"))
        testCartridgeList.set(setOf("ft_test"))
    }
}

val check = tasks.named("check")

subprojects {
    apply(plugin = "org.gradle.test-retry")

    group = rootProject.group
    version = rootProject.version

    // TODO use traditional build output dir until IS-12727 is resolved
    buildDir = File (projectDir, "target")

    repositories.addAll(rootProject.repositories)

    if (!name.endsWith("_test")) {
        apply(plugin = "jacoco")

        extensions.getByType(JacocoPluginExtension::class.java).apply {
            toolVersion = "0.8.5"
        }
    }

    plugins.withType<JavaPlugin> {

        dependencies {
            val cartridge by configurations
            val implementation by configurations
            val testImplementation by configurations

            implementation(platform(project(":versions")))
            testImplementation(platform(project(":versions_test")))

            cartridge(platform("com.intershop.icm:versions:${icmVersion}"))
            implementation(platform("com.intershop.icm:versions:${icmVersion}"))

            cartridge(platform("com.intershop.solrcloud:solrcloud_versions:${solrVersion}"))
            implementation(platform("com.intershop.solrcloud:solrcloud_versions:${solrVersion}"))

            testImplementation(platform("com.intershop.icm:versions_test:${icmVersion}"))
        }

        plugins.withType<com.intershop.gradle.icm.cartridge.TestPlugin> {
            dependencies {
                val implementation by configurations
                implementation(platform("com.intershop.icm:versions_test:${icmVersion}"))
                implementation(platform(project(":versions_test")))
            }
        }

        extensions.getByType(JavaPluginExtension::class.java).apply {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        tasks.withType<JavaCompile> {
            //enable compilation in a separate daemon process
            options.isFork = true
        }

        tasks.withType<Javadoc> {
            if (options is StandardJavadocDocletOptions) {
                val opt = options as StandardJavadocDocletOptions
                // without the -quiet option, the build fails
                opt.addStringOption("Xdoclint:none", "-quiet")
                opt.links("http://docs.oracle.com/en/java/javase/17/docs/api/")
            }
        }

        if(! name.endsWith("_test")) {

            val jacocoReportTask = tasks.named<JacocoReport>("jacocoTestReport")
            jacocoReportTask.configure {
                this.reports {
                    xml.getRequired().set( true )
                    xml.getOutputLocation().set( file("$buildDir/reports/jacoco/xml"))
                }
            }

            tasks.withType<Test> {
                finalizedBy(jacocoReportTask)
            }
        }

        tasks.withType(Test::class.java) {
            jvmArgs("-XX:+HeapDumpOnOutOfMemoryError",
                    "-XX:HeapDumpPath=$buildDir")
        }
    }

    plugins.withType<com.intershop.gradle.icm.docker.ICMGebTestPlugin> {
        tasks.withType(GebTest::class.java) {
            this.mustRunAfter(rootProject.tasks.rebuildSearchIndex, rootProject.tasks.ishUnitTestReport)
        }

        rootProject.tasks.check.configure {
            dependsOn(tasks.named("gebTest", GebTest::class.java))
        }

        extensions.getByType(GebConfiguration::class.java).apply {
            localDriver {
                register("geckoDriver") {
                    osPackages {
                        register("linux") {
                            url.set("https://github.com/mozilla/geckodriver/releases/download/v0.30.0/geckodriver-v0.30.0-linux64.tar.gz")
                            archiveType.set("tar.gz")
                            webDriverExecName.set("geckodriver")
                        }
                        register("win") {
                            url.set("https://github.com/mozilla/geckodriver/releases/download/v0.30.0/geckodriver-v0.30.0-win32.zip")
                            archiveType.set("zip")
                            webDriverExecName.set("geckodriver.exe")
                        }
                        register("mac") {
                            url.set("https://github.com/mozilla/geckodriver/releases/download/v0.30.0/geckodriver-v0.30.0-macos.tar.gz")
                            archiveType.set("tar.gz")
                            webDriverExecName.set("geckodriver")
                        }
                    }
                }
                register("chromeDriver") {
                    osPackages {
                        register("linux") {
                            url.set("https://chromedriver.storage.googleapis.com/97.0.4692.71/chromedriver_linux64.zip")
                            archiveType.set("zip")
                            webDriverExecName.set("chromedriver")
                        }
                        register("win") {
                            url.set("https://chromedriver.storage.googleapis.com/97.0.4692.71/chromedriver_win32.zip")
                            archiveType.set("zip")
                            webDriverExecName.set("chromedriver.exe")
                        }
                        register("mac") {
                            url.set("https://chromedriver.storage.googleapis.com/97.0.4692.71/chromedriver_mac64.zip")
                            archiveType.set("zip")
                            webDriverExecName.set("chromedriver")
                        }
                    }
                }
            }
        }

        tasks.withType<GebTest> {
            useJUnitPlatform()
            retry {
                // the original run and two retries
                maxRetries.set(2)
                // total failures
                maxFailures.set(60)
            }
        }
    }
}

tasks {
    withType<com.intershop.gradle.icm.docker.tasks.BuildImage> {
        mustRunAfter(check)
    }

    check.configure {
        this.dependsOn(ishUnitTestReport)
    }
    register("dockerPublish") {
        dependsOn( pushImages)
        group = "publishing"
    }
}
