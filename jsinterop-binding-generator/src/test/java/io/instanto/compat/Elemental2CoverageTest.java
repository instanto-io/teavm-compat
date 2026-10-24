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
                  exposesMethod(generated, adapted.get(), method, new HashSet<>()));
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

  /** An adapter may live on an inherited interface because TeaVM overlay bodies are final. */
  private boolean exposesMethod(
      Path generated,
      ClassOrInterfaceDeclaration type,
      com.github.javaparser.ast.body.MethodDeclaration method,
      Set<String> seen)
      throws Exception {
    if (type.getMethodsByName(method.getNameAsString()).stream()
        .anyMatch(m -> m.getSignature().equals(method.getSignature()))) return true;
    if (method.isStatic()) return false;
    var unit = type.findCompilationUnit().orElseThrow();
    String pkg = unit.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("");
    var parents = new java.util.ArrayList<>(type.getExtendedTypes());
    parents.addAll(type.getImplementedTypes());
    for (var parent : parents) {
      String name = parent.getNameWithScope();
      String qualified = pkg + name;
      for (var imported : unit.getImports()) {
        if (!imported.isAsterisk() && imported.getName().getIdentifier().equals(name))
          qualified = imported.getNameAsString();
      }
      if (!seen.add(qualified)) continue;
      Path file = generated.resolve(qualified.replace('.', '/') + ".java");
      if (!Files.isRegularFile(file)) continue;
      var parsed = StaticJavaParser.parse(Files.readString(file));
      var declaration =
          parsed
              .getClassByName(parent.getNameAsString())
              .or(() -> parsed.getInterfaceByName(parent.getNameAsString()));
      if (declaration.isPresent() && exposesMethod(generated, declaration.get(), method, seen))
        return true;
    }
    return false;
  }
}
