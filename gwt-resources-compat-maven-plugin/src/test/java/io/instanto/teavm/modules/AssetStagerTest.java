package io.instanto.teavm.modules;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetStagerTest {
  @Rule public TemporaryFolder temporary = new TemporaryFolder();

  @Test
  public void stagesDeclaredJarAssetsWithRelativeFontPathsAndUpdatesExistingFiles()
      throws Exception {
    Path jar =
        jar(
            "widgets.jar",
            "source=META-INF/example\ntarget=assets/example\n",
            Map.of(
                "META-INF/example/css/main.css", "url('../fonts/icon.woff2')",
                "META-INF/example/fonts/icon.woff2", "font",
                "example/Widget.class", "not a public asset"));
    Path site = temporary.newFolder("site").toPath();
    Files.writeString(site.resolve("index.html"), "application");
    assertEquals(2, AssetStager.stage(List.of(jar), site));
    assertEquals("font", Files.readString(site.resolve("assets/example/fonts/icon.woff2")));
    assertEquals(
        "url('../fonts/icon.woff2')",
        Files.readString(site.resolve("assets/example/css/main.css")));
    assertFalse(Files.exists(site.resolve("example/Widget.class")));
    Files.writeString(site.resolve("assets/example/css/main.css"), "old");
    assertEquals(2, AssetStager.stage(List.of(jar), site));
    assertEquals(
        "url('../fonts/icon.woff2')",
        Files.readString(site.resolve("assets/example/css/main.css")));
    assertEquals("application", Files.readString(site.resolve("index.html")));
  }

  @Test
  public void detectsConflictsBeforeWritingAndAcceptsIdenticalSharedAssets() throws Exception {
    String descriptor = "source=public\ntarget=assets/common\n";
    Path one = jar("one.jar", descriptor, Map.of("public/app.js", "one"));
    Path identical = jar("identical.jar", descriptor, Map.of("public/app.js", "one"));
    Path different = jar("different.jar", descriptor, Map.of("public/app.js", "two"));
    Path site = temporary.getRoot().toPath().resolve("conflict-site");
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(one, different), site));
    assertFalse(Files.exists(site));
    assertEquals(1, AssetStager.stage(List.of(one, identical), site));
  }

  @Test
  public void rejectsArchiveTraversalAndInvalidTargetsBeforeWriting() throws Exception {
    Path site = temporary.getRoot().toPath().resolve("unsafe-site");
    Path traversal =
        jar(
            "traversal.jar",
            "source=public\ntarget=assets/widget\n",
            Map.of("public/../../escaped.js", "bad"));
    Path target =
        jar("target.jar", "source=public\ntarget=../outside\n", Map.of("public/app.js", "bad"));
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(traversal), site));
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(target), site));
    assertFalse(Files.exists(site));
    assertFalse(Files.exists(temporary.getRoot().toPath().resolve("escaped.js")));
  }

  @Test
  public void supportsExplodedReactorDependenciesAndIgnoresUnmarkedJars() throws Exception {
    Path classes = temporary.newFolder("classes").toPath();
    Files.createDirectories(classes.resolve("META-INF"));
    Files.writeString(
        classes.resolve(AssetStager.DESCRIPTOR), "source=public\ntarget=assets/reactor\n");
    Files.createDirectories(classes.resolve("public"));
    Files.writeString(classes.resolve("public/app.js"), "reactor");
    Path unmarked = jar("unmarked.jar", null, Map.of("private.js", "private"));
    Path site = temporary.newFolder("reactor-site").toPath();
    assertEquals(1, AssetStager.stage(List.of(classes, unmarked), site));
    assertEquals("reactor", Files.readString(site.resolve("assets/reactor/app.js")));
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(unmarked), site));
  }

  @Test
  public void refusesSymlinksInsideTheDestinationAndMissingDeclaredAssets() throws Exception {
    Path jar =
        jar(
            "linked.jar",
            "source=public\ntarget=assets/widget\n",
            Map.of("public/app.js", "script"));
    Path site = temporary.newFolder("linked-site").toPath();
    Path outside = temporary.newFolder("outside").toPath().toRealPath();
    Files.createSymbolicLink(site.resolve("assets"), outside);
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(jar), site));
    assertFalse(Files.exists(outside.resolve("widget/app.js")));
    Path empty =
        jar(
            "empty.jar",
            "source=absent\ntarget=assets/widget\n",
            Map.of("public/app.js", "script"));
    assertThrows(IOException.class, () -> AssetStager.stage(List.of(empty), outside));
  }

  private Path jar(String name, String descriptor, Map<String, String> contents) throws Exception {
    Path file = temporary.getRoot().toPath().resolve(name);
    Map<String, String> entries = new LinkedHashMap<>(contents);
    if (descriptor != null) entries.put(AssetStager.DESCRIPTOR, descriptor);
    try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(file))) {
      for (var entry : entries.entrySet()) {
        output.putNextEntry(new ZipEntry(entry.getKey()));
        output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
      }
    }
    return file;
  }
}
