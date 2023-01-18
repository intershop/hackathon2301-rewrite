package com.intershop.customization.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.intershop.customization.migration.GradleBuild.Position;

public class GradleBuildTest
{
    private static final Charset BUILD_GRADLE_CHARSET = Charset.forName("utf-8");
    private final GradleBuild underTest = new GradleBuild(null);

    @Test
    void testAll() throws IOException, URISyntaxException
    {
        List<String> lines = Files.readAllLines(Paths.get(getResourceURI("migration/build.gradle.source").toURI()), BUILD_GRADLE_CHARSET);
        String expected = Files.readString(Paths.get(getResourceURI("migration/build.gradle.expected").toURI()), BUILD_GRADLE_CHARSET);
        String result = underTest.migrate(lines);
        assertEquals(expected, result);
    }

    @Test
    void testMapPlugins() throws IOException, URISyntaxException
    {
        List<String> existing = Arrays.asList(
                        "java-cartridge",
                        "static-cartridge",
                        "com.intershop.gradle.cartridge-resourcelist",
                        "com.intershop.gradle.isml"
                        ); 
        List<String> expected = Arrays.asList(
                            "com.intershop.gradle.cartridge-resourcelist",
                            "com.intershop.gradle.isml",
                            "com.intershop.icm.cartridge.external",
                            "com.intershop.icm.cartridge.product",
                            "java"
                        );
        assertEquals(expected, underTest.mapPlugins(existing));
    }

    @Test
    void testDependency()
    {
        List<String> existing = Arrays.asList(
                        "  compile 'commons-collections:commons-collections'",
                        "\tcompile group: 'com.intershop.platform', name: 'ui_web_library'",
                        " "
                        ); 
        List<String> expected = Arrays.asList(
                        "    implementation 'commons-collections:commons-collections'",
                        "    cartridge 'com.intershop.platform:ui_web_library'",
                        ""
                        );
        assertEquals(expected, underTest.convertDependencyLines(existing));
    }
    @Test
    void testListManipulation()
    {
        //                                     0    1    2    3    4    5    6
        List<String> existing = Arrays.asList("b", "{", "1", "2", "3", "}", "a");
        List<String> expectedExtract = Arrays.asList("1", "2", "3");
        List<String> expectedNotIn = Arrays.asList("b", "{", "}", "a");
        assertEquals(expectedExtract, underTest.extract(existing, new Position(2, 4)));
        assertEquals(expectedNotIn, underTest.notIn(existing, new Position(2, 4)));
    }

    private URL getResourceURI(String resourcePath)
    {
        return getClass().getClassLoader().getResource(resourcePath);
    }

}
