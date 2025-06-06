package com.intershop.customization.migration.gradle;

import static com.intershop.customization.migration.common.MigrationContext.OperationType.MODIFY;
import static com.intershop.customization.migration.common.MigrationContext.OperationType.MOVE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.intershop.customization.migration.common.MigrationContext;
import com.intershop.customization.migration.common.MigrationPreparer;
import com.intershop.customization.migration.common.MigrationStep;
import com.intershop.customization.migration.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to migrate used packages in the source files.
 * <p>
 * The configuration is defined in the migration step and contains a map of replacement patterns and a list od allowed
 * file extensions. The keys of the map are the old package names and the values defines the new package names.
 * <p>
 * Example YAML configuration:
 * <pre>
 * type: specs.intershop.com/v1beta/migrate
 * migrator: com.intershop.customization.migration.gradle.RenamedPackages
 * message: "refactor: rename apache packages in source files"
 * options:
 *   package-map:
 *     org.apache.commons.lang: org.apache.commons.lang3
 *     org.apache.commons.collections: org.apache.commons.collections4
 *   file-extension:
 *     - java
 *     - isml
 * </pre>
 */
public class RenamedPackages implements MigrationPreparer
{
    private final static String YAML_KEY_RENAMED_PACKAGES = "package-map";
    private final static String YAML_KEY_FILE_EXTENSION = "file-extension";
    private final static String PACKAGE_SEPARATOR = ".";

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private Map<String, String> renamedPackages = Collections.emptyMap();
    private List<String> allowedFileExtensions = Collections.emptyList();

    @Override
    public void setStep(MigrationStep step)
    {
        Map<String, String> packages = step.getOption(YAML_KEY_RENAMED_PACKAGES);
        if (packages != null && !packages.isEmpty())
        {
            // packages should end with a package separator to ensure correct matching
            this.renamedPackages = new HashMap<>();
            packages.forEach((key, value) -> {
                String pattern = prepareValue(key);
                String replacement = prepareValue(value);
                renamedPackages.put(pattern, replacement);
            });
        }

        this.allowedFileExtensions = step.getOption(YAML_KEY_FILE_EXTENSION);
    }

    // ensure the provided value ends with a package separator
    protected String prepareValue(String value)
    {
        if (!value.endsWith(PACKAGE_SEPARATOR))
        {
            value += PACKAGE_SEPARATOR;
        }

        return value;
    }

    @Override
    public void migrate(Path resource, MigrationContext context)
    {
        LOGGER.debug("Rename packages for cartridge {}.", getResourceName(resource));

        Path srcDir = resource.resolve("src");
        if (!srcDir.toFile().exists() || !srcDir.toFile().isDirectory())
        {
            LOGGER.debug("No source directory found in {}.", srcDir);
            return;
        }

        List<Path> fileList = getFileList(srcDir);
        fileList.forEach(filePath -> processFile(filePath, resource, context));
    }

    // get all files in the src directory and its subdirectories
    // based on the list of allowed file extensions
    protected List<Path> getFileList(Path dir)
    {
        Predicate<Path> filter1 = Files::isRegularFile;
        Predicate<Path> filter2 = this::validateFileExtension;
        Predicate<Path> combined = filter1.and(filter2);

        try
        {
            return FileUtils.listFiles(dir, combined, null);
        }
        catch(IOException e)
        {
            LOGGER.error("An error occurred while retrieving the files in {}.", dir, e);
            throw new RuntimeException(e);
        }
    }

    // process the given file
    // 1. check if the file contains any of the old package names
    // 2. if yes, replace it with the new package name
    protected void processFile(Path filePath, Path resource, MigrationContext context)
    {
        renamedPackages.forEach((key,value) -> {
            if (containsText(filePath, key))
            {
                LOGGER.debug("Replace '{}' with '{}' in '{}'.", key, value, filePath);
                modifyFile(filePath, key, value, resource, context);
            }
        });
    }

    // check if the file contains the old package name
    protected boolean containsText(Path filePath, String text)
    {
        try
        {
            return FileUtils.containsText(filePath, text);
        }
        catch(Exception e)
        {
            LOGGER.error("An error occurred while reading the file {}.", filePath, e);
            throw new RuntimeException(e);
        }
    }

    // replace the old package name with the new package name and write the changes back to the file
    protected void modifyFile(Path filePath, String pattern, String replacement, Path resource, MigrationContext context)
    {
        String cartridgeName = getResourceName(resource);
        try
        {
            List<String> lines = FileUtils.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++)
            {
                String line = lines.get(i);
                if (line.contains(pattern))
                {
                    line = line.replace(pattern, replacement);
                    lines.set(i, line);
                }
            }

            FileUtils.writeLines(filePath, lines);
            context.recordSuccess(cartridgeName, MODIFY, filePath, filePath);
        }
        catch(IOException e)
        {
            LOGGER.error("An error occurred while modifying file {}.", filePath, e);
            context.recordFailure(cartridgeName, MOVE, filePath, filePath,
                    "An error occurred while modifying file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // get the file extension for the provided file
    protected String getFileExtension(Path filePath)
    {
        String fileName = filePath.toFile().getName();
        int lastDotIndex = fileName.lastIndexOf(PACKAGE_SEPARATOR);

        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex + 1) : "";
    }

    // validate the file extension against the list of allowed file extensions
    protected boolean validateFileExtension(Path filePath)
    {
        String fileExtension = getFileExtension(filePath);
        return allowedFileExtensions != null
                        && !allowedFileExtensions.isEmpty()
                        && allowedFileExtensions.stream().anyMatch(fileExtension::equalsIgnoreCase);
    }
}
