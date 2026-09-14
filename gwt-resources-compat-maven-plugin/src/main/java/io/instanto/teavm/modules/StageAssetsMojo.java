package io.instanto.teavm.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Expands declared browser assets from resolved runtime dependencies into a site's root. */
@Mojo(
    name = "stage-assets",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true)
public final class StageAssetsMojo extends AbstractMojo {
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /** Filesystem directory served as the application's web root. */
  @Parameter(
      defaultValue = "${project.build.directory}/site",
      property = "teavm.assets.outputDirectory",
      required = true)
  private File outputDirectory;

  @Override
  public void execute() throws MojoExecutionException {
    List<Path> dependencies =
        project.getArtifacts().stream()
            .filter(a -> "compile".equals(a.getScope()) || "runtime".equals(a.getScope()))
            .map(a -> a.getFile())
            .filter(java.util.Objects::nonNull)
            .map(File::toPath)
            .sorted()
            .toList();
    try {
      int count = AssetStager.stage(dependencies, outputDirectory.toPath());
      getLog().info("Staged " + count + " browser assets into " + outputDirectory);
    } catch (IOException | IllegalArgumentException failure) {
      throw new MojoExecutionException(
          "Unable to stage browser assets: " + failure.getMessage(), failure);
    }
  }
}
