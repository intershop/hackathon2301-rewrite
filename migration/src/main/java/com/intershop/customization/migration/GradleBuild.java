package com.intershop.customization.migration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

public class GradleBuild
{
    private static final Charset CHARSET_BUILD_GRADLE = Charset.defaultCharset();
    private static final String START_DEPENDENCIES = "dependencies";
    private static final String START_INTERSHOP = "intershop";

    private static final String LINE_SEP = System.lineSeparator();
    private static final Predicate<String> IS_OLD_PLUGIN_LINE = (l) -> {
        return l.contains("apply plugin");
    };

    private static final Map<String, String> REPLACE_LINES = Map.of(
                    "zipCartridge.dependsOn lessCompile", "tasks.compileJava.dependsOn(tasks.lessCompile)"
    );
    private static final Map<String, String> PLUGIN_TASK = Map.of(
                    "com.intershop.gradle.isml", "tasks.test.dependsOn(tasks.isml)"
                );
    private final Path projectDir;

    /**
     * Local execution to convert one single build.gradle
     * 
     * @param args
     *            <li>fileName - path to project directory, which contains build.gradle</li>
     */
    public static void main(String[] args)
    {
        GradleBuild migrator = new GradleBuild(new File(args[0]).toPath());
        migrator.migrate();
    }

    public GradleBuild(Path projectDir)
    {
        this.projectDir = projectDir;
    }

    public void migrate()
    {
        Path buildGradle = projectDir.resolve("build.gradle");
        try
        {
            List<String> lines = Files.lines(buildGradle, CHARSET_BUILD_GRADLE).toList();
            String newContent = migrate(lines);
            Files.write(buildGradle, newContent.getBytes(CHARSET_BUILD_GRADLE));
        }
        catch(IOException e)
        {
            LoggerFactory.getLogger(getClass()).error("Can't convert build.gradle", e);
        }
    }

    /**
     * Position to remember location of cough content
     */
    static class Position
    {
        private final int begin;
        private final int end;

        /**
         * Position included
         * @param begin
         * @param end
         */
        Position(int begin, int end)
        {
            this.begin = begin;
            this.end = end;
        }

    }

    /**
     * go step by step through migration steps to fix gradle build
     * 
     * @param lines
     * @return build.gradle content
     * <li>list of plugins
     * <li>intershop block
     * <li>untouched lines
     * <li>new required tasks
     * <li>dependencies
     */
    String migrate(List<String> lines)
    {
        debug("all", lines);
        // collect "semantic" known lines
        List<String> pluginLines = filterPluginLines(lines);
        List<String> newPlugins = mapPluginLines(pluginLines);
        debug("pluginLines", pluginLines);
        List<String> unknownLines = notIn(lines, pluginLines);
        debug("afterPlugin", unknownLines);
        Position dependencyPos = filterBrackets(START_DEPENDENCIES,unknownLines);
        List<String> dependencyLines = extract(unknownLines, dependencyPos);
        debug("dependencyLines", dependencyLines);
        unknownLines = notIn(unknownLines, dependencyPos);
        debug("afterDependency", unknownLines);
        Position intershopPos = filterBrackets(START_INTERSHOP,unknownLines);
        List<String> intershopLines = extract(unknownLines, intershopPos);
        debug("intershopLines", intershopLines);
        unknownLines = notIn(unknownLines, intershopPos);
        debug("afterIntershop", unknownLines);
        unknownLines = mapNewTasksForOldTasks(unknownLines);

        // build result
        StringBuilder result = new StringBuilder();
        // add plugins
        result = result.append(joinPlugins(newPlugins)).append(LINE_SEP);
        // add intershop block (descriptions)
        result = result.append(intershopBlock(intershopLines)).append(LINE_SEP);
        // add all own known lines
        result = result.append(String.join(LINE_SEP, unknownLines)).append(LINE_SEP);
        // collect tasks for plugins
        result = result.append(joinTasksForNewPlugins(newPlugins)).append(LINE_SEP);
        // put dependencies to the end
        result = result.append(migrateDependencies(dependencyLines));
        return result.toString();
    }

    /**
     * @param newPlugins of project
     * @return content for required tasks by given new plugins
     */
    private String joinTasksForNewPlugins(List<String> newPlugins)
    {
        if (newPlugins.isEmpty())
        {
            return "";
        }
        List<String> tasks = new ArrayList<>(); 
        for(String plugin : newPlugins)
        {
            if (PLUGIN_TASK.containsKey(plugin))
            {
                tasks.add(PLUGIN_TASK.get(plugin));
            }
        }
        return String.join(LINE_SEP, tasks) + LINE_SEP;
    }

    /**
     * @param lines
     * @return lines which contains replaced tasks, if there was an mapping at REPLACE_LINES
     */
    private List<String> mapNewTasksForOldTasks(List<String> lines)
    {
        List<String> tasks = new ArrayList<>(); 
        for(String line : lines)
        {
            String replacement = REPLACE_LINES.get(line.trim());
            tasks.add(replacement != null ? replacement : line);
        }
        return tasks;
    }

    private void debug(String step, List<String> lines)
    {
        int pos = 0;
        for (String line : lines)
        {
            LoggerFactory.getLogger(getClass()).trace("d:{} {} Line: {}", pos++, step, line);
        }
    }

    /**
     * @param lines of intershop block
     * @return description from intershop extension block
     */
    private String intershopBlock(List<String> lines)
    {
        String description = null;
        for (String line : lines)
        {
            // displayName = 'Application - Responsive Starter Store'
            if (line.contains("displayName"))
            {
                String[] parts = line.split("'");
                description = parts[1];
            }
        }
        if (description != null)
        {
            return "description = '" + description + "'" + LINE_SEP;
        }
        return "";
    }

    /**
     * @param lines all lines
     * @param find position of find lines
     * @return lines which are not included in position
     */
    List<String> notIn(List<String> lines, Position find)
    {
        List<String> result = new ArrayList<>();
        result.addAll(lines.subList(0, find.begin));
        result.addAll(lines.subList(find.end + 1, lines.size()));
        return result;
    }

    /**
     * @param lines all lines
     * @param find position of find lines
     * @return lines which are included in position
     */
    List<String> extract(List<String> lines, Position find)
    {
        if (find.begin > -1 && find.end >= find.begin)
        {
            return lines.subList(find.begin, find.end + 1);
        }
        return Collections.emptyList();
    }

    /**
     * @param startMarker block marker
     * @param lines
     * @return position for given block
     */
    private Position filterBrackets(String startMarker, List<String> lines)
    {
        int startIntershop = -1;
        int counterBrackets = 0;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line.startsWith(startMarker))
            {
                startIntershop = i;
            }
            if (startIntershop > 0)
            {
                if (line.contains("{"))
                {
                    counterBrackets++;
                }
                if (line.contains("}"))
                {
                    counterBrackets--;
                    if (counterBrackets == 0)
                    {
                        return new Position(startIntershop, i);
                    }
                }
            }
        }
        return new Position(-1, -1);
    }

    /**
     * Migrate dependency lines (starting with "dependencies {") ends with "}"
     * @param lines
     * @return content for new dependencies
     */
    private String migrateDependencies(List<String> lines)
    {
        StringBuilder b = new StringBuilder();
        b = b.append("// TODO please validate that cartridges have \"cartridge\" as dependency declaration instead of \"implementation\".").append(LINE_SEP);
        b = b.append(lines.get(0)).append(LINE_SEP);
        convertDependencyLines(lines.subList(1, lines.size() - 1));
        for (int i = 1; i < lines.size() - 1; i++)
        {
            String converted = convertDependencyLine(lines.get(i));
            if (!converted.isEmpty())
            {
                b.append("    ");
            }
            b = b.append(converted).append(LINE_SEP);
        }
        b = b.append(lines.get(lines.size() - 1)).append(LINE_SEP);
        return b.toString();
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
        // convert to standard gradle configurations
        String converted = depLine.replace("compile", "implementation")
                                  .replace("runtime", "runtimeOnly")
                                  .replace("testCompile", "testImplementation")
                                  .replace("testRuntime", "testRuntimeOnly")
                                  .trim();
        if (converted.contains("group:"))
        {
            String[] partsImpl = converted.split("group:");
            String[] partsDep = converted.split("'");
            converted = partsImpl[0] + "'" + partsDep[1] + ":" + partsDep[3] + "'";
        }
        // use cartridge in case we assume it's a cartridge
        String[] parts = converted.split("'");
        if (parts[1].startsWith("com.intershop.platform")
                        || parts[1].startsWith("com.intershop.content")
                        || parts[1].startsWith("com.intershop.business")
                        || parts[1].startsWith("com.intershop.b2b")
                        )
        {
            converted = converted
                            .replace("implementation", "cartridge")
                            .replace("runtimeOnly", "cartridgeRuntime")
                            .trim();
            converted = converted.replace("implementation", "cartridge");
        }
        return converted.trim();
    }

    /**
     * @param lines all lines
     * @param filtered must be unique otherwise the line is removed twice
     * @return lines without filtered lines
     */
    private List<String> notIn(List<String> lines, List<String> filtered)
    {
        return lines.stream().filter(s -> !filtered.contains(s)).toList();
    }

    /**
     * @param lines all lines
     * @return lines with "plugin apply:"
     */
    private List<String> filterPluginLines(List<String> lines)
    {
        return lines.stream().filter(IS_OLD_PLUGIN_LINE).toList();
    }

    /**
     * @param plugins
     * @return string for build.gradle block of defined plugins
     */
    private String joinPlugins(List<String> plugins)
    {
        StringBuilder b = new StringBuilder();
        b = b.append("plugins {").append(LINE_SEP);
        for (String plugin : plugins)
        {
            b = b.append("    id \'" + plugin + "\'\n");
        }
        b.append("}").append(LINE_SEP);
        return b.toString();
    }

    /**
     * Plugins can be removed
     */
    private static final List<String> PLUGIN_REMOVED = Arrays.asList("static-cartridge");

    /**
     * Plugins mapped
     */
    private static final Map<String, String> PLUGIN_MAP = Map.of("java-cartridge", "java");
    /**
     * Plugins leave at it is
     */
    private static final List<String> PLUGIN_UNTOUCHED = Arrays.asList(
                    "com.intershop.gradle.cartridge-resourcelist",
                    "com.intershop.gradle.isml");
    /**
     * Plugins added/replaced
     */
    private static final Map<String, List<String>> PLUGIN_ADDED = Map.of(
                    "java-cartridge", Arrays.asList("com.intershop.icm.cartridge.product", "com.intershop.icm.cartridge.external"
                                    )
                    );

    List<String> mapPluginLines(List<String> lines)
    {
        return mapPlugins(lines.stream().map(this::extractPluginFromLine).toList());
    }

    List<String> mapPlugins(List<String> oldPlugins)
    {
        List<String> result = new ArrayList<>();
        for (String existingPlugin : oldPlugins)
        {
            boolean processedPlugin = false;
            if (PLUGIN_MAP.containsKey(existingPlugin))
            {
                result.add(PLUGIN_MAP.get(existingPlugin));
                processedPlugin = true;
            }
            if (PLUGIN_UNTOUCHED.contains(existingPlugin))
            {
                result.add(existingPlugin);
                processedPlugin = true;
            }
            if (PLUGIN_REMOVED.contains(existingPlugin))
            {
                processedPlugin = true;
                // ignore
            }
            if (PLUGIN_ADDED.containsKey(existingPlugin))
            {
                result.addAll(PLUGIN_ADDED.get(existingPlugin));
                processedPlugin = true;
            }
            if (!processedPlugin)
            {
                result.add(existingPlugin);
                LoggerFactory.getLogger(getClass()).warn("Unknow plugin '{}' was added.", existingPlugin);
            }
        }
        result.sort((a, b) -> a.compareTo(b));
        return result;
    }

    private String extractPluginFromLine(String line)
    {
        String[] parts = line.split("'");
        if (parts.length == 1) parts = line.split("\"");
        return parts[1];
    }

}
