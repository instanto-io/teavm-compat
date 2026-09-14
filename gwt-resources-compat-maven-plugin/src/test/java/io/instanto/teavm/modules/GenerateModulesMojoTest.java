package io.instanto.teavm.modules;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GenerateModulesMojoTest {
  @Rule public TemporaryFolder temporary = new TemporaryFolder();

  @Test
  public void inlineTextResourcesKeepLocalesOutOfStartupScripts() throws Exception {
    Path source = temporary.newFolder("inline-source").toPath();
    Path directory = source.resolve("example/client");
    Files.createDirectories(directory);
    Files.writeString(directory.resolve("Editor.gwt.xml"), "<module/>");
    Files.writeString(
        directory.resolve("Locales.java"),
        """
                package example.client;
                public interface Locales extends ClientBundle {
                  @Source("fr.js") TextResource french();
                }
                """);
    String script = "// quote \\\" slash \\\\ newline\n" + "x".repeat(70000);
    Files.writeString(directory.resolve("fr.js"), script);
    Path output = temporary.newFolder("inline-java").toPath();
    Path classes = temporary.newFolder("inline-classes").toPath();
    GenerateModulesMojo mojo = new GenerateModulesMojo();
    set(mojo, "sourceRoot", source.toFile());
    set(mojo, "outputRoot", output.toFile());
    set(mojo, "classOutput", classes.toFile());
    set(mojo, "assetRoot", temporary.newFolder("inline-site"));
    set(mojo, "packagedRoot", temporary.newFolder("inline-jar"));
    set(mojo, "resourcesClass", "example.client.Assets");
    set(mojo, "idPrefix", "example-");
    set(mojo, "inlineTextBundles", List.of("example.client.Locales"));
    mojo.execute();
    assertFalse(
        Files.readString(output.resolve("example/client/EditorResources.java")).contains("fr.js"));
    assertEquals(
        "example.client.Locales_TeaVM\n",
        Files.readString(classes.resolve("META-INF/services/example.client.Locales")));
    String provider = Files.readString(output.resolve("example/client/Locales_TeaVM.java"));
    assertTrue(provider.contains("TextResource french()"));
    assertTrue(provider.contains("text.append("));
    assertTrue(provider.length() > script.length());
  }

  @Test
  public void packagesStylesheetsAndScriptsWithPresenceChecks() throws Exception {
    Path source = temporary.newFolder("module-source").toPath();
    Path directory = source.resolve("example/client");
    Files.createDirectories(directory);
    Files.writeString(
        directory.resolve("Theme.gwt.xml"),
        """
                <module>
                  <public path="public"/>
                  <stylesheet src="css/theme.css"/>
                  <script src="js/core.js"/>
                  <script src="js/extension.js"/>
                </module>
                """);
    List<String> resources =
        List.of(
            "js/core.js",
            "js/extension.js",
            "css/theme.css",
            "css/theme.css.map",
            "fonts/icons.woff2");
    for (String resource : resources) {
      Path file = directory.resolve("public/" + resource);
      Files.createDirectories(file.getParent());
      Files.writeString(file, "fixture " + resource);
    }
    Path output = temporary.newFolder("generated").toPath();
    Path assets = temporary.newFolder("assets").toPath();
    Path packaged = temporary.newFolder("packaged").toPath();
    GenerateModulesMojo mojo = new GenerateModulesMojo();
    set(mojo, "sourceRoot", source.toFile());
    set(mojo, "outputRoot", output.toFile());
    set(mojo, "assetRoot", assets.toFile());
    set(mojo, "packagedRoot", packaged.toFile());
    set(mojo, "resourcesClass", "example.client.Resources");
    set(mojo, "includeModules", List.of("Theme"));
    set(mojo, "idPrefix", "example-");
    set(mojo, "scriptPresence", Map.of("core.js", "example.client.Resources::isCoreLoaded"));
    mojo.execute();
    String java = Files.readString(output.resolve("example/client/ThemeResources.java"));
    assertTrue(java.contains("Resources::isCoreLoaded"));
    assertTrue(java.indexOf("core.js") < java.indexOf("extension.js"));
    for (String resource : resources) {
      assertEquals("fixture " + resource, Files.readString(packaged.resolve(resource)));
      assertEquals(-1L, Files.mismatch(assets.resolve(resource), packaged.resolve(resource)));
    }
  }

  private static void set(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void packagesGenericModuleAndBundleAssetsWithoutBootstrapConventions() throws Exception {
    Path source = temporary.newFolder("source").toPath();
    Path directory = source.resolve("example/client");
    Files.createDirectories(directory);
    Files.writeString(
        directory.resolve("Editor.gwt.xml"),
        """
                <module>
                  <public path="public">
                    <include name="**/*"/>
                    <exclude name="private/**"/>
                  </public>
                  <stylesheet src="skin/editor.css"/>
                  <script src="vendor/core.js"/>
                  <script src="vendor/editor.js"/>
                </module>
                """);
    Files.writeString(
        directory.resolve("EditorAssets.java"),
        """
                package example.client;
                interface EditorAssets extends ClientBundle {
                  @Source("internal/extension.js") TextResource extension();
                  @Source({"icons/one.svg", "icons/two.svg"}) DataResource icons();
                  @Source("strings/messages.json") TextResource messages();
                }
                """);
    for (String file :
        List.of(
            "public/skin/editor.css",
            "public/vendor/core.js",
            "public/vendor/editor.js",
            "public/images/icon.png",
            "public/private/secret.txt",
            "internal/extension.js",
            "icons/one.svg",
            "icons/two.svg",
            "strings/messages.json")) {
      Path path = directory.resolve(file);
      Files.createDirectories(path.getParent());
      Files.writeString(path, "fixture asset");
    }
    Path output = temporary.newFolder("java").toPath();
    Path packaged = temporary.newFolder("jar").toPath();
    GenerateModulesMojo mojo = new GenerateModulesMojo();
    set(mojo, "sourceRoot", source.toFile());
    set(mojo, "outputRoot", output.toFile());
    set(mojo, "assetRoot", temporary.newFolder("site"));
    set(mojo, "packagedRoot", packaged.toFile());
    set(mojo, "resourcesClass", "example.client.Assets");
    set(mojo, "idPrefix", "example-");
    mojo.execute();
    String java = Files.readString(output.resolve("example/client/EditorResources.java"));
    assertTrue(java.contains(".cssBase() + \"../skin/editor.css\""));
    assertTrue(java.indexOf("vendor/core.js") < java.indexOf("vendor/editor.js"));
    assertTrue(java.indexOf("vendor/editor.js") < java.indexOf("internal/extension.js"));
    assertFalse(java.contains(".script(Assets.jsBase() + \"../strings/messages.json\")"));
    for (String file :
        List.of(
            "skin/editor.css",
            "vendor/core.js",
            "vendor/editor.js",
            "images/icon.png",
            "internal/extension.js",
            "icons/one.svg",
            "icons/two.svg",
            "strings/messages.json")) {
      assertTrue(file, Files.isRegularFile(packaged.resolve(file)));
    }
    assertFalse(Files.exists(packaged.resolve("private/secret.txt")));
  }
}
