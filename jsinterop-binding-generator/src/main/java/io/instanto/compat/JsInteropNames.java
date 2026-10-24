package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import java.util.*;

/** Resolves source constants used as JsInterop names before emitting JavaScript bindings. */
final class JsInteropNames {
  private final Map<String, Expression> constants = new HashMap<>();

  JsInteropNames(Collection<String> sources) {
    for (String source : sources) {
      var cu = StaticJavaParser.parse(source);
      for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
        for (var field : owner.getFields()) {
          if (!(owner.isInterface() || field.isStatic() && field.isFinal())) continue;
          for (var variable : field.getVariables()) {
            if (!variable.getTypeAsString().equals("String")) continue;
            variable
                .getInitializer()
                .ifPresent(
                    value ->
                        constants.put(
                            owner.getFullyQualifiedName().orElseThrow()
                                + "."
                                + variable.getNameAsString(),
                            value));
          }
        }
      }
    }
  }

  String transform(String source) {
    var cu = StaticJavaParser.parse(source);
    boolean changed = false;
    for (var annotation : cu.findAll(NormalAnnotationExpr.class)) {
      if (!Set.of("JsType", "JsMethod", "JsProperty")
          .contains(annotation.getName().getIdentifier())) continue;
      for (var pair : annotation.getPairs()) {
        if (!Set.of("name", "namespace").contains(pair.getNameAsString())) continue;
        Expression value = pair.getValue();
        if (value.isStringLiteralExpr()) continue;
        String resolved = resolve(value, new HashSet<>());
        if (resolved == null)
          throw new IllegalArgumentException(
              "Cannot resolve JsInterop "
                  + pair
                  + " in "
                  + cu.getPrimaryTypeName().orElse("source"));
        pair.setValue(new StringLiteralExpr().setString(resolved));
        changed = true;
      }
    }
    return changed ? cu.toString() : source;
  }

  private String resolve(Expression expression, Set<String> visiting) {
    if (expression.isStringLiteralExpr()) return expression.asStringLiteralExpr().asString();
    if (expression.isEnclosedExpr())
      return resolve(expression.asEnclosedExpr().getInner(), visiting);
    if (expression.isBinaryExpr()
        && expression.asBinaryExpr().getOperator() == BinaryExpr.Operator.PLUS) {
      String left = resolve(expression.asBinaryExpr().getLeft(), visiting);
      String right = resolve(expression.asBinaryExpr().getRight(), visiting);
      return left == null || right == null ? null : left + right;
    }
    if (!expression.isNameExpr() && !expression.isFieldAccessExpr()) return null;
    String name = expression.toString();
    var cu = expression.findCompilationUnit().orElseThrow();
    List<String> candidates = new ArrayList<>();
    candidates.add(name);
    for (Node node = expression; node != null; node = node.getParentNode().orElse(null)) {
      if (node instanceof ClassOrInterfaceDeclaration owner)
        candidates.add(owner.getFullyQualifiedName().orElseThrow() + "." + name);
    }
    for (var imp : cu.getImports()) {
      String imported = imp.getNameAsString();
      if (imp.isAsterisk()) candidates.add(imported + "." + name);
      else {
        String simple = imp.getName().getIdentifier();
        if (name.equals(simple)) candidates.add(imported);
        if (name.startsWith(simple + "."))
          candidates.add(imported + name.substring(simple.length()));
      }
    }
    cu.getPackageDeclaration().ifPresent(p -> candidates.add(p.getNameAsString() + "." + name));
    for (String candidate : candidates) {
      if (candidate.equals("jsinterop.annotations.JsPackage.GLOBAL")) return "<global>";
      Expression value = constants.get(candidate);
      if (value != null && visiting.add(candidate)) {
        String resolved = resolve(value, visiting);
        visiting.remove(candidate);
        if (resolved != null) return resolved;
      }
    }
    return null;
  }
}
