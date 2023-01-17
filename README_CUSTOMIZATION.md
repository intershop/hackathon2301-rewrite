How to Work with an ICM AppServer Customer Project
==================================================

[TOC]: #

# Table of Contents
- [Description](#description)
- [Prerequisites](#prerequisites)
- [Git Checkout](#git-checkout)
- [Prepare Customization Project In Azure](#prepare-customization-project-in-azure)
- [Set Up Customization Project](#set-up-customization-project)

# Description

- This project contains the templates of a customization project. 
- The customization project will be created beside this project.
  - <someDir>/icm-as-customization-template
  - <someDir>/<projectDirectory>
  - the `projectDirectory` is defined in `configuration.properties`
- the keys used in the `configuration.properties` file match the files with in the placeholders

| configuration.properties | template file | 
|--------------------------|---------------|
| someKey                  | <@someKey@>   |

- there are following template file, which are used to generate the required project files

| template files           | created files       | 
|--------------------------|---------------------|
| azure-pipelines.yml.tmpl | azure-pipelines.yml |
| build.gradle.kts.tmpl    | build.gradle.kts    |
| gradle.properties.tmpl   | gradle.properties   |
| settings.gradle.kts.tmpl | settings.gradle.kts |


# Prerequisites

- bash with rsync
- git is installed

# Git Checkout

Clone directory:

    cd <target directory>
    git clone ggit@ssh.dev.azure.com:v3/intershop-com/Products/icm-as-customization-template
    # or
    git clone https://intershop-com@dev.azure.com/intershop-com/Products/_git/icm-as-customization-template

For a detailed description, see https://docs.microsoft.com/en-us/azure/devops/user-guide/code-with-git?view=azure-devops

# Prepare Customization Project In Azure

- create a repository in azure
  - the name should match the value of the key `projectDirectory` in the `configuration.properties` file
  - the project must be empty (uncheck the README box)
- copy the remote url (ssh) for later use

# Set Up Customization Project

- copy properties template
  ```shell script
  cp configuration.properties.sample configuration.properties
  ```
- fill in input properties in `configuration.properties`
- execute (to provide rsync to set permissions)
  ```shell script
  sudo su -
  ```
- run the shell script in the project directory
  ```shell script
  ./initializeProject.sh
  ```
- navigate to the newly created customization project
  - it should only contain an initial git commit
  - *.tmpl and `configuration.properties` related files should be removed
- set the remote url
  ```shell script
  git remote set-url origin <ssh-url>
  ```
  - use the previously copied url from [Prepare Azure](#prepare-customization-project-in-azure) for `<ssh-url>`
- push to the new repository
  ```shell script
  git push --set-upstream origin master
  ```
