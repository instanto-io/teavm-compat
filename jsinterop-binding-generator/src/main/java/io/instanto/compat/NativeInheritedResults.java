package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.*;

/** Inherit a generic result bridge once, rather than overriding a TeaVM overlay method. */
final class NativeInheritedResults {
  private record Declaration(CompilationUnit unit, ClassOrInterfaceDeclaration type) {}

  private final Map<String, Declaration> types = new HashMap<>();

  NativeInheritedResults(Collection<String> sources) {
    for (String source : sources) {
      CompilationUnit unit = StaticJavaParser.parse(source);
      for (var type : unit.getTypes()) {
        if (type.isClassOrInterfaceDeclaration()) {
          var declaration = type.asClassOrInterfaceDeclaration();
          types.put(
              qualified(unit, declaration.getNameAsString()), new Declaration(unit, declaration));
        }
      }
    }
  }

  String transform(String source) {
    CompilationUnit unit = StaticJavaParser.parse(source);
    boolean changed = false;
    for (var top : unit.getTypes()) {
      if (!top.isClassOrInterfaceDeclaration()) continue;
      var owner = top.asClassOrInterfaceDeclaration();
      if (!isNative(owner)) continue;
      Set<String> inherited = new HashSet<>();
      collectParents(new Declaration(unit, owner), inherited, new HashSet<>());
      for (var method : new ArrayList<>(owner.getMethods())) {
        if ((method.isNative() || owner.isInterface() && method.getBody().isEmpty())
            && GwtNativeBoundary.isErasedResult(method)
            && inherited.contains(signature(owner, method))) {
          method.remove();
          changed = true;
        }
      }
    }
    return changed ? unit.toString() : source;
  }

  private void collectParents(Declaration child, Set<String> signatures, Set<String> visited) {
    List<com.github.javaparser.ast.type.ClassOrInterfaceType> parents = new ArrayList<>();
    parents.addAll(child.type().getExtendedTypes());
    parents.addAll(child.type().getImplementedTypes());
    for (var parent : parents) {
      String name = resolve(child.unit(), parent.getNameWithScope());
      if (!visited.add(name)) continue;
      Declaration declaration = types.get(name);
      if (declaration == null || !isNative(declaration.type())) continue;
      for (var method : declaration.type().getMethods()) {
        if ((method.isNative() || declaration.type().isInterface() && method.getBody().isEmpty())
            && GwtNativeBoundary.isErasedResult(method))
          signatures.add(signature(declaration.type(), method));
      }
      collectParents(declaration, signatures, visited);
    }
  }

  private static String signature(ClassOrInterfaceDeclaration owner, MethodDeclaration method) {
    Set<String> variables = new HashSet<>();
    owner.getTypeParameters().forEach(t -> variables.add(t.getNameAsString()));
    method.getTypeParameters().forEach(t -> variables.add(t.getNameAsString()));
    return method.getNameAsString()
        + method.getParameters().stream()
            .map(
                p -> {
                  String type = p.getTypeAsString();
                  return (variables.contains(type) ? "Object" : type) + (p.isVarArgs() ? "[]" : "");
                })
            .toList();
  }

  private String resolve(CompilationUnit unit, String name) {
    if (types.containsKey(name)) return name;
    for (var entry : unit.getImports()) {
      if (!entry.isAsterisk() && entry.getName().getIdentifier().equals(name))
        return entry.getNameAsString();
    }
    return qualified(unit, name);
  }

  private static String qualified(CompilationUnit unit, String name) {
    return unit.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("") + name;
  }

  private static boolean isNative(ClassOrInterfaceDeclaration type) {
    return type.getAnnotationByName("JsType")
        .map(
            a ->
                a.isNormalAnnotationExpr()
                    && a.asNormalAnnotationExpr().getPairs().stream()
                        .anyMatch(
                            p ->
                                p.getNameAsString().equals("isNative")
                                    && p.getValue().toString().equals("true")))
        .orElse(false);
  }
}
