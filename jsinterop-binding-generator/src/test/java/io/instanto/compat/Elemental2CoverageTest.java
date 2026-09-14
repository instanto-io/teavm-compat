package io.instanto.compat;

import static org.junit.Assert.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.net.JarURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** Verifies generation coverage against every Java source in the upstream modules. */
public class Elemental2CoverageTest {
  @Test
  public void everyPinnedDeclarationIsGenerated() throws Exception {
    String[] representatives = {
      "core/JsArray",
      "dom/Document",
      "promise/Promise",
      "svg/SVGSVGElement",
      "webstorage/Storage",
      "indexeddb/IDBFactory",
      "webgl/WebGLRenderingContext",
      "media/OfflineAudioContext"
    };
    Set<String> expected = new HashSet<>();
    Path generated = Path.of("../target/compat/elemental2");
    for (String representative : representatives) {
      var resource =
          getClass().getClassLoader().getResource("elemental2/" + representative + ".java");
      assertNotNull(representative, resource);
      var connection = (JarURLConnection) resource.openConnection();
      connection.setUseCaches(false);
      try (var jar = connection.getJarFile()) {
        for (var entry : jar.stream().filter(e -> e.getName().endsWith(".java")).toList()) {
          String path = entry.getName();
          assertTrue("Duplicate input " + path, expected.add(path));
          assertTrue(
              "Missing generated source " + path, Files.isRegularFile(generated.resolve(path)));
          String original;
          try (var input = jar.getInputStream(entry)) {
            original = new String(input.readAllBytes(), StandardCharsets.UTF_8);
          }
          var before = StaticJavaParser.parse(original);
          var after = StaticJavaParser.parse(Files.readString(generated.resolve(path)));
          for (var declaration : before.findAll(ClassOrInterfaceDeclaration.class)) {
            var adapted =
                after.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(c -> c.getNameAsString().equals(declaration.getNameAsString()))
                    .findFirst();
            assertTrue(
                path + ": missing type " + declaration.getNameAsString(), adapted.isPresent());
            for (var field : declaration.getFields()) {
              for (var variable : field.getVariables()) {
                assertTrue(
                    path + ": missing field " + variable.getNameAsString(),
                    adapted.get().getFieldByName(variable.getNameAsString()).isPresent());
              }
            }
            for (var method : declaration.getMethods()) {
              // Native overlay casts are supplied by TeaVM's inherited JSObject.cast().
              if (method.getNameAsString().equals("cast")
                  && method.isAnnotationPresent("JsOverlay")) continue;
              assertTrue(
                  path + ": missing method " + method.getDeclarationAsString(),
                  adapted.get().getMethodsByName(method.getNameAsString()).stream()
                      .anyMatch(
                          m ->
                              m.getParameters()
                                  .toString()
                                  .equals(method.getParameters().toString())));
            }
          }
        }
      }
    }
    try (var files = Files.walk(generated)) {
      Set<String> actual = new HashSet<>();
      files
          .filter(p -> p.toString().endsWith(".java"))
          .forEach(p -> actual.add(generated.relativize(p).toString()));
      assertEquals(
          "Generated source set must match the declared upstream inputs", expected, actual);
    }
  }
}
