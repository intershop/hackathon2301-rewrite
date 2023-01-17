How to Work with an ICM AppServer Customer Project
==================================================

[TOC]: #

# Table of Contents
- [Prerequisites](#prerequisites)
- [Git Checkout](#git-checkout)
- [Set environment](#set-environment)
- [Component Configurations](#component-configurations)
  - [WebAdapter and WebAdapterAgent](#webadapter-and-webadapteragent)
  - [Database and Database preparation](#database)
  - [Application Server](#application-server)
  - [Solr Server and ZooKeeper](#solr-server-and-zookeeper)
  - [Mail Server](#mail-server)
- [ICM Test](#icm-tests)
  - [ISH Unit Tests](#ish-unit-tests)
  - [Rest Tests](#rest-tests)
  - [Geb Tests](#geb-tests)
- [Workflow - Overview](#workflow)
- [Gradle Tasks Overview](#gradle-tasks-overview)
- [IStudio](#istudio)
- [Links for local running applications](#links-for-local-running-applications)

## Prerequisites

- Installed [AdoptOpenJDK 11 HotSpot](https://adoptopenjdk.net)
- Installed Container Runtime
  - Windows: [How to ...](https://docs.docker.com/desktop/install/windows-install/)
  - Mac: [How to ...](https://docs.docker.com/desktop/install/mac-install/)
  - Linux: [How to ...](https://docs.docker.com/desktop/install/linux-install/)
- IDE with extension for container/Docker ([Intershop Studio](http://studio.intershop.de), VSCode, IntellJ)
- Basic knowledge of docker, docker-compose and docker networking (https://docs.docker.com)

- Access to:
  * Azure DevOps project (https://dev.azure.com/<@adoOrganization@>/<@adoProject@>)
  * Azure Artifacts Maven project repositories (https://dev.azure.com/<@adoOrganization@>/<@adoProject@>/_artifacts/feed/icm-maven-artifacts)
  - Docker registry with the ICM base containers (https://docker.tools.intershop.com)
  - Docker registry with project containers (<@customerRegistry@>)

- Basic Docker Configuration
  - 8 GB RAM are recommended (Settings: *Resources* | *Advanced*)
  - File sharing of the drive/folder used for development must be enabled (Settings: *Resources* | *File Sharing*)
  - Expose the daemon on port 2375 without TLS (Settings: *General*)

__Docker login__

  > Do not forget to run a docker login with your account for the necessary repositories:
  > Login for private DockerHub images:
  > ```
  > docker login <Docker repository>
  > ```
  >
  > Retrieve credentials from docker.tools.intershop.com, see step by step [Guide - Access to Intershop Docker Images](https://intershop.atlassian.net/wiki/spaces/ENFDEVDOC/pages/48254976693/Guide+-+Access+to+Intershop+Docker+Images)
  > Login for docker.tools.intershop.com:
  > ```
  > docker login docker.tools.intershop.com -u <XXX@user-intershop.de> -p <SuperStrongCLISecret>
  > ```
  >
  > Login for Docker project registry
  > ```
  > az login
  > az acr login --name <@customerRegistry@>
  > ```
  > For more details see [Microsoft Azure Documentation](https://learn.microsoft.com/en-us/azure/container-registry/container-registry-authentication?tabs=azure-cli)
## Git Checkout

Clone directory:

    cd <target directory>
    git clone git@ssh.dev.azure.com:v3/<@adoOrganization@>/<@adoProject@>/<@adoProject@>
    # or
    git clone https://intershop-com@dev.azure.com/<@adoOrganization@>/<@adoProject@>/_git/<@adoProject@>

    # branch for Gradle update and Containerization
    git checkout master

For a detailed description, see https://docs.microsoft.com/en-us/azure/devops/user-guide/code-with-git?view=azure-devops

## Set Environment

1. Optional: Set your `GRADLE_USER_HOME` via:

    ```batch
    SET GRADLE_USER_HOME=<DEV_HOME>/gradle
    ```

    or

    ```shell
    export GRADLE_USER_HOME=<DEV_HOME>/gradle
     ```

2. Create a file *&lt;GRADLE_USER_HOME&gt;/icm-default/conf/icm.properties*. See https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#PropertiesFile for an example.

4. Put *license.xml* file to  *&lt;GRADLE_USER_HOME&gt;/icm-default/lic*.

  Notes:

  > It is possible to change the location of both files with system or
  > environment properties or project properties:
  >
  > - License directory:
  >   - System property: `licenseDir`
  >   - Project property: `licenseDir`
  >   - Environment variable: `LICENSEDIR`
  > - Configuration directory:
  >   - System property: `configDir`
  >   - Project property: `configDir`
  >   - Environment variable: `CONFIGDIR`
  >
  > Therefore you can use variables in a *gradle.properties* file:
  >
  >     systemProp.gradle.licenseDir = <path to dir>
  >     systemProp.gradle.configDir = <path to dir>
  >
  > or set environment variables:
  >
  > ```batch
  > SET LICENSEDIR=<path to dir>
  > SET CONFIGDIR=<path to dir>
  > ```
  >
  > ```shell
  > export LICENSEDIR=<path to dir>
  > export CONFIGDIR=<path to dir>
  > ```

5. Create a file *&lt;GRADLE_USER_HOME&gt;/gradle.properties* (for more details see [Gradle Documentation](https://docs.gradle.org/current/userguide/build_environment.html)). Add the following entries

    ```
    repoUser = <Azure login>
    repoPassword = <Azure Personal Access Token>
    ```

__Azure Personal Access Token__

  > You need also an Personal Access Token for the Azure Artifacts access
  > How to create [personal access token](https://docs.microsoft.com/en-us/azure/devops/organizations/accounts/use-personal-access-tokens-to-authenticate?view=azure-devops&tabs=Windows)


## Component Configurations

For tests with ICM it is necessary to start additional components:
- Intershop WebAdapter and Webadapter Agent
- Solr Server and ZooKeeper
- Mail Server
- Database

The application server must be started also in the container.

### WebAdapter and WebAdapterAgent
#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#WAConfiguration) are defined

#### Starting and stopping
* see tasks `*WA`, `*WAA`, `*WebServer` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#DockerPluginTasks

#### Use TLS certificates inside the WebAdapter

* Obtain wildcard certificates:
    * you can use [Let's encrypt](https://letsencrypt.org)
    * for an easy setup in a team it is advisable to use wild card certificates

* Prepare your host:
    * determine the IP of your host inside the Intershop network e.g.
        * on Windows execute
          ```batch
          ipconfig /all
          ```
        * on Linux execute
          ```shell
          ip address
          ```
        * pick the IP assigned to your LAN or WiFi adapter (not those of virtual network adapters)
    * map your fully qualified hostname to the IP (has to be renewed every time your IP changed e.g. enable VPN)
        * add/modify a line inside the `hosts`-file matching the pattern: `<IP> <fullyQualifiedHostname>` e.g.
          ```
          10.0.206.81 jmmustermannnb.domainname.com
          ```
        * location of `hosts`-file
            * Windows: `C:\Windows\System32\drivers\etc\hosts`
            * Linux/Mac: `/etc/hosts`


* Modify your [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#WAConfiguration) and set the following 3 properties  

      webServer.cert.path = <certificateCheckoutPath>/<ChosenDomain>
      webserver.cert.server = fullchain.pem
      webserver.cert.privatekey = privkey.pem

### Database and Database preparation

Generally the build supports a database started using the provided gradle tasks `*MSSQL` (see https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#DockerPluginTasks) or alternatively an externally started database.

#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#DBPrepareConfiguration) are defined
* the database is [started](#database)

#### Execution
* See task `dbPrepare` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#CustomizationDockerPluginTasks.

### Application Server

#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#StartASConfiguration) are defined
* the database is [started](#database) and properly prepared

#### Starting and stopping
* See tasks `startAS` and `stopAS` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#CustomizationDockerPluginTasks.

### Solr Server and ZooKeeper

**Attention**: as of version `34.0.0` (and below) there's no external Solr server supported (only the one startable by build script)

#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#SolrConfiguration) are defined

#### Starting and stopping
* See tasks `*Solr`, `*ZK` and `*SolrCloud` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#DockerPluginTasks.

#### Manage search index content
* cleanup the indexes (requires a running Zookeeper, Solr server and AppServer)
  ```shell
  ./gradlew cleanUpSolr
  ```
* rebuild the indexes (requires a running Zookeeper, Solr server, [WebAdapter](#webadapter-and-webadapteragent) and [Application Server](#application-server))
  ```shell
  ./gradlew rebuildSearchIndex
  ```
* Solr server backoffice <br/> navigate to `http://localhost:<solr.port>/solr/`
* **Attention:** search index lifecycle <br/> the search index has the same lifecycle as the Solr server container - when the container is stopped the index is lost so a rebuild is necessary after each Solr server (re-)start

### Mail Server

#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#MailConfiguration) are defined

#### Starting and stopping
* See tasks `*MailSrv` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#DockerPluginTasks.

#### Mail Server Administration
* The configuration uses a container of [MailHog](https://github.com/mailhog/MailHog). For more information see the GitHub page or https://mailtrap.io/blog/mailhog-explained/

## ICM Tests
### ISH Unit Tests
#### Prerequisites
* relevant [`icm.properties`](https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#ISHUnitTestConfiguration) are defined
* the [Solr server + Zookeeper](#solr-server-and-zookeeper) are started
* the database is [started](#database) and properly prepared
* the [application](#application-server) server is **NOT** started

#### Execution
* See tasks `ishUnitTestReport` and `*ISHUnitTest` at https://github.com/IntershopCommunicationsAG/icm-docker-plugin/blob/master/README.asciidoc#CustomizationDockerPluginTasks.

#### Define a new test suite:
* edit the file `build.gradle.kts`
    * add a new `register`-block in into `intershop_docker.ishUnitTests` defining
        * test suite name
        * test cartridge name
        * the test suite class
        * for instance
          ```
          register("my_cartridge_test") {
              cartridge.set("my_cartridge_test")
              testSuite.set("tests.embedded.com.intershop.mypackage.SuiteMyTests")
          }
          ```

### Rest Tests

There are currently no Rest Tests available in the demo store.

#### Prerequisites
* the [Solr server + Zookeeper](#solr-server-and-zookeeper) are started
* the database is [started](#database) and properly prepared
* the [WebAdapter](#webadapter-and-webadapteragent) is started and uses [TLS certificates](#use-tls-certificates-inside-the-webadapter)
* the [application server](#application-server) is started
* the [search index](#manage-search-index-content) has been built

#### Execution of all tests:
  ```shell
  gradlew restTest
  ```
In case of failing tests the reports for each project containing executed rest tests can be found at
  ```
  <buildRoot>/<projectName>/target/reports/tests/restTest
  ``` 

#### Execution of tests contained inside a certain project:
  ```shell
  gradlew <projectName>:restTest
  ```

#### Execution of tests specified by pattern:
* the task `restTest` `is of type org.gradle.api.tasks.testing.Test` and therefore supports the [--tests](https://docs.gradle.org/current/userguide/java_testing.html#test_filtering) parameter
* for instance
  ```shell
  gradlew restTest --tests=*SuggestRestSpec*
  ```
  In case of failing tests the reports for each project containing executed rest tests can be found at
  ```
  <buildRoot>/<projectName>/target/reports/tests/restTest
  ```
  
### Geb Tests
#### Prerequisites
* the [Solr server + Zookeeper](#solr-server-and-zookeeper) are started
* the database is [started](#database) and properly prepared
* the [WebAdapter](#webadapter-and-webadapteragent) is started and uses [TLS certificates](#use-tls-certificates-inside-the-webadapter)
* the [application server](#application-server) is started
* the [search index](#manage-search-index-content) has been built
* a running and configured mail server (see [Mail Server](#mail-server) )

#### Execution of all tests:
  ```shell
  gradlew gebTest
  ```
In case of failing tests the reports for each project containing executed tests can be found at
  ```
  <buildRoot>/<projectName>/target/reports/tests/gebTest
  ``` 

#### Execution of tests contained inside a certain project:
  ```shell
  gradlew <projectName>: gebTest
  ```

## Workflow - Overview

1. Checkout the repository.

2. Adapt your configuration.

3. Start your database

4. Run `./gradlew startServer` (`dbPrepare` is embedded).

`dbPrepare` and `startServer` combine all compile tasks.

## Gradle Tasks Overview

This is a short overview over the main tasks. For more detailed
information, refer to:
- [icm-gradle-plugin](https://github.com/IntershopCommunicationsAG/icm-gradle-plugin)
- [icm-docker-plugin](https://github.com/IntershopCommunicationsAG/icm-docker-plugin)

- **createServerInfoProperties**
  * The task creates a file: *$buildDir/tempConf/version.properties*.
  * This file contains version information of the assembly.
  * This is a replacement of the former *ivy.xml* of assembly.

- **installConfFiles**
  * The task collects conf files of a development environment into
    *$buildDir/conf*.
  * This is combined copy from *icm_config*.

- **createServerDirProperties**
  * The task creates a *properties* file including information about several files in the build directory.
  * The *properties* file stores information on:
    * Directory with licence file
    * Configuration directory path
    * Sites folder path
    * etc.
  * It replaces the former configuration in different share and local directories.

- **dbPrepare**
  * This task executes a `dbPrepare`.
  * It collect all necessary paths and configuration for `dbPrepare`.
  * This is the replacement of the former command `dbinit`.

  * This command runs in a container. All necessary files and
    directories will be mounted by this container.

- **ishUnitTestReport**
  * This task executes all ISH Unit tests and creates a report.

    `b2bISHUnitTest` starts the ISH Unit tests from the configuration b2b.

  * The command runs in a container. All necessary files and directories
    will be mounted by this container.

- **startServer**
  * This task starts web server, web adapter agent und application server.
    Web server and web adapter agent are started in the Docker runtime on the local machine.
  * This task starts `startAS` and `startWebServer`.
  * Add parameter `--debug-jvm` for opening the debug port 5005.

- **startAS**
  * This task starts the application server from sources.
  * It collects all necessary paths and configuration for the start up.
  * Add parameter `--debug-jvm` for opening the debug port 5005.

- **stopServer**
  * This task stops web server, web adapter agent und application server.
  * This task stops `stopAS` and `stopWebServer`.

- **stopAS**
  * This tasks stops the running application server.
  * It collect all necessary paths and configuration for the stop process.

- **startMailSrv / stopMailSrv**
  * These tasks start / stop the MailHog SMTP server, see [MailHog at GitHub](https://github.com/mailhog/MailHog).

- **startMSSQL / stopMSSQL**
  * These tasks start a containerized MSSQL server prepared for ICM.

- **startSolrCloud / stopSolrCloud**
  * These tasks start Zookeeper und Solr images for a single node SolrCloud instance.

- **rebuildSearchIndex**
  * This task calls the application server job to recreate the search index.
  * It starts bevor a task to remove the old collections and configurations.
  * This task will be called automatically by the gebTest task.

- **getTest**
  * Runs the Geb+Spock tests with the tests container framework by default.
  * If the server is not started all necessary containers will be started.

- **buildImages**
  * Creates all available Docker images.

- **containerClean**
  * Removes all containers started for the current project.

## IStudio

Version >= IntershopStudio_4.17.0.30

### Preparation

- Verify AdoptOpenJDK 11 is used.
- Configure the directories, which contain the *icm.properties* and the *license.xml*, before starting IStudio:

Windows:
```batch
SET LICENSEDIR=<path to dir>
SET CONFIGDIR=<path to dir>
 ```
Linux:
```shell
export LICENSEDIR=<path to dir>
export CONFIGDIR=<path to dir>
 ```

### Debug Pipelines

The HTTP and HTTPS port of the pipeline debug configuration must match to the
HTTP and HTTPS port in the *icm.properties* file.

### Debug Java Code

The task `dbPrepare`, `testIshUnit`, `startAS` and `startServer` can be started with the option `--debug-icm`. 
Supported values are: 
- true/yes: enable debugging
- suspend: enable debugging in suspend-mode
- every other value: - disable debugging

The IDE can connect via port `7746` (customizable via `icm.properties/intershop.as.debug.port`).

## Links for local running applications

  * [SMC](http://localhost:8080/INTERSHOP/web/BOS/SMC/-/-/-/SMCMain-Start)
  * [Backoffice](http://localhost:8080/INTERSHOP/web/WFS/SLDSystem)
  * [inSPIRED Site](http://localhost:8080/INTERSHOP/web/WFS/inSPIRED-inTRONICS-Site/en_US/-/USD/)
  * [inSPIRED Business Site](http://localhost:8080/INTERSHOP/web/WFS/inSPIRED-inTRONICS_Business-Site/en_US/-/USD/)
