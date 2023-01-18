package com.intershop.customization.migration;

import java.nio.file.Path;

import com.intershop.beehive.tools.StaticFileMigrator;

public class MoveArtifacts
{

    private Path projectDir;

    public MoveArtifacts(Path projectDir)
    {
        this.projectDir = projectDir;
    }

    public void migrate()
    {
        String args[] = { projectDir.toAbsolutePath().toString() };
        StaticFileMigrator.main(args);
    }

}
