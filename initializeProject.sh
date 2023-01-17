#!/bin/bash

# keys used in this script are matching with the scripts in configuration.properties.sample
# therefore they are matching configuration.properties file

# outputFileNames
settingsGradleFile="settings.gradle.kts"
gradlePropertiesFile="gradle.properties"
buildGradleFile="build.gradle.kts"
azurePipelineFile="azure-pipelines.yml"
readmeFile="README.md"

# the template files match there corresponding output files and append this suffix
# inputFileName=$outputFileNames$templateSuffix
templateSuffix=".tmpl"

configurationFile="configuration.properties"
fileLocation="${PWD}/$configurationFile"

function getPropertyValue {
    grep "${1}" ${fileLocation} | cut -d'=' -f2 | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//'
}

subDirectory=${PWD}
echo "Source Dir : " $subDirectory

if [ "$(getPropertyValue 'configurationProjectDirectory')" == "" ]; then
  configTargetDirectory=$subDirectory/../$(getPropertyValue 'projectDirectory')-ci-configuration
else
  configTargetDirectory=$subDirectory/../$(getPropertyValue 'configurationProjectDirectory')
fi
echo "The project configuration is stored in " $configTargetDirectory

targetDirectory=$subDirectory/../$(getPropertyValue 'projectDirectory')
echo "Target Dir : "$targetDirectory


# first argument the file with placeholders
# remaining are the keys
createReplaceCommand () {
  echo "Target file :" $1
  replaceString="sed"
  for i in "${@:2}"
  do
    value=$(getPropertyValue "${i}")
    replaceString="$replaceString -e \"s/<@${i}@>/${value}/g\" "
  done
  replaceString="$replaceString $subDirectory/${1}${templateSuffix} > $targetDirectory/${1}"
  echo "Replacement String :" $replaceString
  eval $replaceString
}

initGit() {
  cd $1

  git init
  echo "Git init for $1."

  git add -A

  export message="Initial Setup for $(getPropertyValue 'rootProject')"

  git commit --no-edit -m "$message"

  git log HEAD  
}

# exclude git, template, configuration files and README_CUSTOMIZATION
rsync -a --exclude ".git/" \
  --exclude "build/" \
  --exclude "*.tmpl" \
  --exclude "$configurationFile" \
  --exclude "$configurationFile".sample \
  --exclude "README_CUSTOMIZATION.md" \
  --exclude "initializeProject.sh" \
  --exclude "icm.properties" \
  "$subDirectory"/ "$targetDirectory"

echo "Rsync finished. "

allOutputFile="$targetDirectory/$settingsGradleFile $targetDirectory/$gradlePropertiesFile \
               $targetDirectory/$buildGradleFile $targetDirectory/$azurePipelineFile $targetDirectory/$readmeFile"

# settings.gradle.kts
createReplaceCommand $settingsGradleFile "rootProject"

# gradle.properties
createReplaceCommand $gradlePropertiesFile "icmVersion" "solrVersion" "headlessVersion" \
      "customerRegistry" "icmWAVersion" "icmWAAgentVersion" \
      "adoOrganization" "adoProject"

# build.gradle.kts
createReplaceCommand $buildGradleFile "description" "group" "productID" \
      "productName" "copyrightOwner" "copyrightFrom" "organization"

# azure-pipelines.yml
createReplaceCommand $azurePipelineFile "projectDirectory" "adoProject" "rootProject"

# README.md
createReplaceCommand $readmeFile "adoOrganization" "adoProject" "customerRegistry"

echo "Following placeholders found after rename:"
if grep "<@.*@>" $allOutputFile; then
  echo "Error: not all placeholders were found - exit"
  exit 1
else
  echo "All placeholders were replaced"
fi

mkdir $configTargetDirectory
cp ${subDirectory}/icm.properties $configTargetDirectory

initGit $targetDirectory

initGit $configTargetDirectory
