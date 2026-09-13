package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import java.util.*;

/** Bridges GWT DOM wrappers at native method boundaries while retaining the public Java API. */
public final class GwtNativeBoundary {
  private GwtNativeBoundary() {}

  static void adapt(CompilationUnit cu) {
    Map<String, String> imported = new HashMap<>();
    cu.getImports().forEach(i -> imported.put(i.getName().getIdentifier(), i.getNameAsString()));
    for (ClassOrInterfaceDeclaration owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      boolean nativeOwner = owner.getAnnotationByName("JSClass").isPresent();
      int index = 0;
      for (MethodDeclaration method : new ArrayList<>(owner.getMethods())) {
        if (!method.isNative()) continue;
        boolean body = method.getAnnotationByName("JSBody").isPresent();
        if (!nativeOwner && !body) continue;
        boolean boundary =
            isGwt(method.getTypeAsString(), imported)
                || (body && !nativeOwner && !method.isStatic());
        for (Parameter p : method.getParameters()) boundary |= isGwt(p.getTypeAsString(), imported);
        if (!boundary) continue;
        MethodDeclaration bridge = method.clone();
        bridge
            .setName("$gwtBridge" + index++)
            .setPublic(false)
            .setProtected(false)
            .setPrivate(true);
        String jsName = method.getNameAsString();
        var annotation = method.getAnnotationByName("JSMethod");
        if (annotation.isPresent() && annotation.get().isSingleMemberAnnotationExpr())
          jsName =
              annotation
                  .get()
                  .asSingleMemberAnnotationExpr()
                  .getMemberValue()
                  .asStringLiteralExpr()
                  .asString();
        bridge.getAnnotations().removeIf(a -> a.getNameAsString().equals("JSMethod"));
        if (!body)
          bridge.addAnnotation(StaticJavaParser.parseAnnotation("@JSMethod(\"" + jsName + "\")"));
        if (body && !nativeOwner) {
          var jsBody = method.getAnnotationByName("JSBody").orElseThrow().asNormalAnnotationExpr();
          for (var pair : jsBody.getPairs()) {
            if (pair.getNameAsString().equals("script")
                && pair.getValue().asStringLiteralExpr().asString().matches("(?s).*\\bthis\\b.*"))
              throw new IllegalArgumentException(
                  "JSNI receiver needs an explicit bridge: "
                      + owner.getNameAsString()
                      + "."
                      + method.getNameAsString());
          }
          bridge.setStatic(true);
        }
        StringBuilder setup = new StringBuilder();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < method.getParameters().size(); i++) {
          Parameter p = method.getParameter(i);
          String n = p.getNameAsString();
          if (isGwt(p.getTypeAsString(), imported)) {
            boolean array = p.isVarArgs() || p.getType().isArrayType();
            String rawType = nativeType(p.getTypeAsString(), imported);
            bridge.getParameter(i).setType(rawType);
            if (array) {
              String base = rawType.replace("[]", "");
              setup
                  .append(base)
                  .append("[] $raw")
                  .append(i)
                  .append(" = ")
                  .append(n)
                  .append(" == null ? null : new ")
                  .append(base)
                  .append("[")
                  .append(n)
                  .append(".length];")
                  .append("if (")
                  .append(n)
                  .append(" != null) for(int j=0;j<")
                  .append(n)
                  .append(".length;j++) $raw")
                  .append(i)
                  .append("[j] = ")
                  .append(n)
                  .append("[j] == null ? null : ")
                  .append(n)
                  .append("[j].unwrap();");
              args.add("$raw" + i);
            } else args.add(n + " == null ? null : " + n + ".unwrap()");
          } else args.add(n);
        }
        String call = bridge.getNameAsString() + "(" + String.join(",", args) + ")";
        if (isGwt(method.getTypeAsString(), imported)) {
          String type = method.getTypeAsString();
          bridge.setType(nativeType(type, imported));
          if (method.getType().isArrayType()) {
            String base = type.replace("[]", "");
            setup
                .append(bridge.getTypeAsString())
                .append(" $result = ")
                .append(call)
                .append("; if($result==null) return null;")
                .append(type)
                .append(" $wrapped = new ")
                .append(base)
                .append("[$result.length]; for(int j=0;j<$result.length;j++) $wrapped[j]=")
                .append(wrap(base, "$result[j]", imported))
                .append(";return $wrapped;");
          } else setup.append("return ").append(wrap(type, call, imported)).append(";");
        } else
          setup.append(method.getType().isVoidType() ? "" : "return ").append(call).append(";");
        owner.addMember(bridge);
        method.setNative(false);
        method
            .getAnnotations()
            .removeIf(
                a ->
                    Set.of("JSMethod", "JSProperty", "JSTopLevel", "JSBody")
                        .contains(a.getNameAsString()));
        method.setBody(StaticJavaParser.parseBlock("{" + setup + "}"));
      }
    }
  }

  private static String qualified(String type, Map<String, String> imports) {
    String base = type.replace("[]", "");
    return imports.getOrDefault(base, base);
  }

  private static boolean isGwt(String type, Map<String, String> imports) {
    return Set.of(
            "com.google.gwt.dom.client.Element",
            "com.google.gwt.dom.client.Node",
            "com.google.gwt.core.client.JavaScriptObject",
            "com.google.gwt.core.client.JsDate")
        .contains(qualified(type, imports));
  }

  private static String nativeType(String type, Map<String, String> imports) {
    String base =
        switch (qualified(type, imports)) {
          case "com.google.gwt.dom.client.Element" -> "org.teavm.jso.dom.html.HTMLElement";
          case "com.google.gwt.dom.client.Node" -> "org.teavm.jso.dom.xml.Node";
          default -> "org.teavm.jso.JSObject";
        };
    return base + (type.endsWith("[]") ? "[]" : "");
  }

  private static String wrap(String type, String value, Map<String, String> imports) {
    return switch (qualified(type, imports)) {
      case "com.google.gwt.dom.client.Element" ->
          "com.google.gwt.dom.client.Element.as(com.google.gwt.core.client.JavaScriptObject.of("
              + value
              + "))";
      case "com.google.gwt.core.client.JavaScriptObject" ->
          "com.google.gwt.core.client.JavaScriptObject.of(" + value + ")";
      case "com.google.gwt.core.client.JsDate" ->
          "com.google.gwt.core.client.JsDate.of(" + value + ")";
      case "com.google.gwt.dom.client.Node" -> "com.google.gwt.dom.client.Node.wrap(" + value + ")";
      default -> throw new IllegalArgumentException("Unsupported native GWT return: " + type);
    };
  }
}
