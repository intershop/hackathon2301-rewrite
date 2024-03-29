package com.intershop.customization.migration.gradle;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.intershop.customization.migration.common.MigrationPreparer;
import com.intershop.customization.migration.common.MigrationStep;
import com.intershop.customization.migration.common.Position;

public class RenamedDependency implements MigrationPreparer
{
    private static final String YAML_KEY_RENAMED_DEPENDENCY = "dependency-map";
    private static final Charset CHARSET_BUILD_GRADLE = Charset.defaultCharset();
    private static final String START_DEPENDENCIES = "dependencies";
    private static final String LINE_SEP = System.lineSeparator();

    private Map<String, String> renamedDependencies = Collections.emptyMap();

    @Override
    public void migrate(Path projectDir)
    {
        Path buildGradle = projectDir.resolve("build.gradle");
        try
        {
            List<String> lines = Files.lines(buildGradle, CHARSET_BUILD_GRADLE).toList();
            Files.write(buildGradle, migrate(lines).getBytes(CHARSET_BUILD_GRADLE));
            LoggerFactory.getLogger(getClass()).info("build.gradle converted at {}.", projectDir);
        }
        catch(IOException e)
        {
            LoggerFactory.getLogger(getClass()).error("Can't convert build.gradle", e);
        }
    }

    @Override
    public void setStep(MigrationStep step)
    {
        this.renamedDependencies = step.getOption(YAML_KEY_RENAMED_DEPENDENCY);
    }
    /**
     * go step by step through migration steps to fix gradle build
     * @param lines
     * @return build.gradle content
     */
    String migrate(List<String> lines)
    {
        Position dependencyPos = Position.findBracketBlock(START_DEPENDENCIES, lines).orElse(Position.NOT_FOUND(lines));
        List<String> unknownLines = dependencyPos.nonMatchingLines();
        List<String> dependencyLines = dependencyPos.matchingLines();

        // build result
        StringBuilder result = new StringBuilder();
        // add all own known lines
        result = result.append(String.join(LINE_SEP, unknownLines)).append(LINE_SEP);
        // collect tasks for plugins
        // put dependencies to the end
        result = result.append(String.join(LINE_SEP, convertDependencyLines(dependencyLines))).append(LINE_SEP);
        return result.toString();
    }

    /**
     * @param lines original dependency lines without "header" and "footer"
     * @return converted lines
     */
    List<String> convertDependencyLines(List<String> lines)
    {
        return lines.stream().map(this::convertDependencyLine).toList();
    }

    /**
     * @param lines original dependency line
     * @return converted line
     */
    private String convertDependencyLine(String depLine)
    {
        if (depLine.trim().isEmpty())
        {
            return depLine.trim();
        }
        // use cartridge in case we assume it's a cartridge
        String[] parts = depLine.split("'");
        if (parts.length < 2)
        {
            return depLine;
        }
        // convert to standard gradle configurations
        String converted = depLine;
        if (renamedDependencies.containsKey(parts[1]))
        {
            converted = converted.replace(parts[1], renamedDependencies.get(parts[1]));
        }
        return converted;
    }
}
