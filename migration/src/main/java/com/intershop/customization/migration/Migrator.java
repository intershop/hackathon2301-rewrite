package com.intershop.customization.migration;

import java.io.File;
import java.nio.file.Path;

public class Migrator
{
    private static final int POS_TASK = 0;
    private static final int POS_PATH = 1;
    /**
     * @param args
     * <li>"project" as task</li>
     * <li>directory to project hackathon2301-rewrite/app_sf_responsive</li>
     */
    public static void main(String[] args)
    {
        if (args.length == POS_PATH + 1)
        {
            if ("project".equals(args[POS_TASK]))
            {
                migrateProject(new File(args[POS_PATH]));
            }
            else if ("projects".equals(args[POS_TASK]))
            {
                migrateProjects(new File(args[POS_PATH]));
            }
        }
        else
        {
            System.err.printf("Missing parameter %d.", args.length );
        }
        return;
    }

    /**
     * Migrate on root project
     * @param projectDir
     */
    private static void migrateProjects(File projectDir)
    {
        // TODO traverse directories with build.gradle files
    }

    /**
     * Migrate on project
     * @param projectDir
     */
    private static void migrateProject(File projectDir)
    {
        Migrator migrator = new Migrator(projectDir.toPath());
        migrator.migrate("7.10", "11.0.9");
    }

    private final GradleBuild gradleBuildMigrator;
    private final MoveArtifacts moveArtifacts;

    public Migrator(Path path)
    {
        this.gradleBuildMigrator = new GradleBuild(path);
        this.moveArtifacts = new MoveArtifacts(path);
    }

    private void migrate(String string, String string2)
    {
        moveArtifacts.migrate();
        gradleBuildMigrator.migrate();
    }

}
