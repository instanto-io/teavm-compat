package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Adapts direct GWT overlay subclasses while leaving their canonical sources GWT-compatible. */
final class GwtOverlayTypes {
  private final Set<String> types = new HashSet<>();

  GwtOverlayTypes(Collection<String> sources) {
    for (String source : sources) {
      var cu = StaticJavaParser.parse(source);
      boolean imported =
          cu.getImports().stream()
              .anyMatch(
                  i ->
                      i.getNameAsString().equals("com.google.gwt.core.client.JavaScriptObject")
                          || i.isAsterisk()
                              && i.getNameAsString().equals("com.google.gwt.core.client"));
      String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("");
      for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
        if (owner.getExtendedTypes().stream()
            .anyMatch(
                t ->
                    t.getNameWithScope().equals("com.google.gwt.core.client.JavaScriptObject")
                        || imported && t.getNameAsString().equals("JavaScriptObject"))) {
          types.add(pkg + owner.getNameAsString());
        }
      }
    }
  }

  Set<String> types() {
    return Set.copyOf(types);
  }

  String transform(String source) {
    if (types.isEmpty()) return source;
    var cu = StaticJavaParser.parse(source);
    String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("");
    boolean changed = false;
    for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String name = owner.getNameAsString();
      if (!types.contains(pkg + name)) continue;
      if (owner.isNestedType())
        throw new IllegalArgumentException("Nested GWT overlays are not supported: " + name);
      for (var constructor : owner.getConstructors()) {
        if (!constructor.getParameters().isEmpty() || !constructor.getBody().isEmpty())
          throw new IllegalArgumentException(
              "GWT overlays require an empty no-arg constructor: " + name);
        constructor.setBody(StaticJavaParser.parseBlock("{super(null);}"));
      }
      owner.addMember(
          StaticJavaParser.parseBodyDeclaration(
              "private " + name + "(org.teavm.jso.JSObject value) {super(value);}"));
      owner.addMember(
          StaticJavaParser.parseBodyDeclaration(
              "public static "
                  + name
                  + " $wrapNative(org.teavm.jso.JSObject value) {"
                  + "return value == null ? null : new "
                  + name
                  + "(value);}"));
      changed = true;
    }
    return changed ? cu.toString() : source;
  }
}
