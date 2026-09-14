package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.*;
import java.util.*;

/** Adapts generic JsFunction arguments using their original functional declarations. */
public final class NativeCallbackBindings {
  private record Function(
      List<String> variables, String method, List<String> args, String result) {}

  private final Map<String, Function> functions = new LinkedHashMap<>();

  public NativeCallbackBindings(Collection<String> declarations) {
    for (String source : declarations) {
      var cu = StaticJavaParser.parse(source);
      for (var type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
        if (type.getAnnotationByName("JsFunction").isEmpty()) continue;
        var methods =
            type.getMethods().stream().filter(m -> !m.isStatic() && m.getBody().isEmpty()).toList();
        if (methods.size() != 1)
          throw new IllegalArgumentException(
              "Expected single JsFunction method: " + type.getName());
        var method = methods.get(0);
        String name = type.getFullyQualifiedName().orElseThrow();
        functions.put(
            name,
            new Function(
                type.getTypeParameters().stream().map(t -> t.getNameAsString()).toList(),
                method.getNameAsString(),
                method.getParameters().stream().map(p -> p.getTypeAsString()).toList(),
                method.getTypeAsString()));
      }
    }
  }

  public String transform(String source) {
    var cu = StaticJavaParser.parse(source);
    Map<String, String> imports = new HashMap<>();
    cu.getImports().forEach(i -> imports.put(i.getName().getIdentifier(), i.getNameAsString()));
    boolean changed = false;
    for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      int next = 0;
      for (var method : new ArrayList<>(owner.getMethods())) {
        if (!method.isNative() || method.getAnnotationByName("JSBody").isPresent()) continue;
        List<Shape> shapes = new ArrayList<>();
        for (var p : method.getParameters()) shapes.add(shape(p.getType(), imports));
        Shape result = shape(method.getType(), imports);
        if (result == null && shapes.stream().allMatch(Objects::isNull)) continue;
        String suffix = "$callback" + next++;
        var bridge = method.clone().setName(method.getNameAsString() + suffix);
        bridge.getAnnotations().clear();
        String nativeName =
            method
                .getAnnotationByName("JSMethod")
                .filter(a -> a.isSingleMemberAnnotationExpr())
                .map(
                    a ->
                        a.asSingleMemberAnnotationExpr()
                            .getMemberValue()
                            .asStringLiteralExpr()
                            .asString())
                .orElse(method.getNameAsString());
        bridge.addAnnotation(
            StaticJavaParser.parseAnnotation("@org.teavm.jso.JSMethod(\"" + nativeName + "\")"));
        List<String> args = new ArrayList<>();
        for (int i = 0; i < shapes.size(); i++) {
          var p = method.getParameter(i);
          Shape shape = shapes.get(i);
          String name = p.getNameAsString();
          if (shape == null) {
            args.add(name);
            continue;
          }
          String raw = "$Raw" + suffix + "_" + i;
          declare(owner, raw, shape);
          bridge.getParameter(i).setType(raw);
          List<String> vars = new ArrayList<>(), values = new ArrayList<>();
          for (int j = 0; j < shape.args.size(); j++) {
            vars.add("a" + j);
            values.add(inbound(shape.args.get(j), "a" + j, imports));
          }
          String call = name + "." + shape.function.method + "(" + String.join(",", values) + ")";
          String lambda =
              "(" + String.join(",", vars) + ") -> " + outbound(shape.result, call, imports);
          String key = shape.key.replace("\"", "\\\"");
          args.add(
              name
                  + " == null ? null : io.instanto.compat.NativeCallbacks.remember("
                  + name
                  + ",\""
                  + key
                  + "\",("
                  + raw
                  + ")("
                  + lambda
                  + "))");
        }
        String call = bridge.getNameAsString() + "(" + String.join(",", args) + ")";
        String body;
        if (result != null) {
          String raw = "$Raw" + suffix + "_result";
          declare(owner, raw, result);
          bridge.setType(raw);
          List<String> vars = new ArrayList<>(), values = new ArrayList<>();
          for (int j = 0; j < result.args.size(); j++) {
            vars.add("a" + j);
            values.add(outbound(result.args.get(j), "a" + j, imports));
          }
          String invoke = "nativeFunction.call(" + String.join(",", values) + ")";
          body =
              "{ "
                  + raw
                  + " nativeFunction="
                  + call
                  + "; return nativeFunction == null ? null : ("
                  + String.join(",", vars)
                  + ") -> "
                  + inbound(result.result, invoke, imports)
                  + "; }";
        } else body = "{" + (method.getType().isVoidType() ? "" : "return ") + call + ";}";
        owner.addMember(bridge);
        method.setNative(false);
        method.getAnnotations().removeIf(a -> a.getName().getIdentifier().equals("JSMethod"));
        if (owner.isInterface()) method.setDefault(true);
        method.setBody(StaticJavaParser.parseBlock(body));
        changed = true;
      }
    }
    return changed ? cu.toString() : source;
  }

  private record Shape(Function function, List<String> args, String result, String key) {}

  private Shape shape(Type type, Map<String, String> imports) {
    if (!type.isClassOrInterfaceType()) return null;
    var declared = type.asClassOrInterfaceType();
    String name = declared.getNameWithScope();
    var matches =
        functions.entrySet().stream()
            .filter(e -> e.getKey().equals(name) || e.getKey().endsWith("." + name))
            .toList();
    if (matches.isEmpty()) return null;
    if (matches.size() != 1) throw new IllegalArgumentException("Ambiguous callback: " + name);
    Function function = matches.get(0).getValue();
    var parameters = declared.getTypeArguments().orElse(new com.github.javaparser.ast.NodeList<>());
    if (!parameters.isEmpty() && parameters.size() != function.variables.size())
      throw new IllegalArgumentException("Unresolved callback: " + type);
    Map<String, String> variables = new HashMap<>();
    for (int i = 0; i < function.variables.size(); i++)
      variables.put(
          function.variables.get(i),
          parameters.isEmpty() ? "Object" : parameters.get(i).toString());
    var args = function.args.stream().map(t -> variables.getOrDefault(t, t)).toList();
    String result = variables.getOrDefault(function.result, function.result);
    if (args.stream().noneMatch(t -> needsBridge(t, imports)) && !needsBridge(result, imports))
      return null;
    String key =
        matches.get(0).getKey()
            + args.stream().map(t -> qualified(t, imports)).toList()
            + qualified(result, imports);
    return new Shape(function, args, result, key);
  }

  private static boolean needsBridge(String type, Map<String, String> imports) {
    return Set.of(
            "Object",
            "java.lang.Object",
            "Double",
            "Integer",
            "Boolean",
            "com.google.gwt.dom.client.Element",
            "com.google.gwt.core.client.JavaScriptObject")
        .contains(qualified(type, imports));
  }

  private static String qualified(String type, Map<String, String> imports) {
    return imports.getOrDefault(type, type);
  }

  private static String raw(String type) {
    return switch (type) {
      case "void", "boolean", "byte", "short", "int", "long", "float", "double", "char", "String" ->
          type;
      default -> "org.teavm.jso.JSObject";
    };
  }

  private static void declare(ClassOrInterfaceDeclaration owner, String name, Shape shape) {
    StringJoiner parameters = new StringJoiner(",");
    for (int i = 0; i < shape.args.size(); i++) parameters.add(raw(shape.args.get(i)) + " a" + i);
    owner.addMember(
        StaticJavaParser.parseBodyDeclaration(
            "@org.teavm.jso.JSFunctor public interface "
                + name
                + " extends org.teavm.jso.JSObject { "
                + raw(shape.result)
                + " call("
                + parameters
                + "); }"));
  }

  private static String inbound(String type, String value, Map<String, String> imports) {
    return switch (qualified(type, imports)) {
      case "Integer", "java.lang.Integer" ->
          "java.lang.Integer.valueOf(jsinterop.base.Js.asInt(" + value + "))";
      case "Double", "java.lang.Double" ->
          "java.lang.Double.valueOf(jsinterop.base.Js.asDouble(" + value + "))";
      case "Boolean", "java.lang.Boolean" ->
          "java.lang.Boolean.valueOf(jsinterop.base.Js.asBoolean(" + value + "))";
      case "com.google.gwt.dom.client.Element" ->
          "com.google.gwt.dom.client.Element.as(com.google.gwt.core.client.JavaScriptObject.of("
              + value
              + "))";
      case "com.google.gwt.core.client.JavaScriptObject" ->
          "com.google.gwt.core.client.JavaScriptObject.of(" + value + ")";
      case "void", "boolean", "byte", "short", "int", "long", "float", "double", "char", "String" ->
          value;
      default -> "jsinterop.base.Js.cast(" + value + ")";
    };
  }

  private static String outbound(String type, String value, Map<String, String> imports) {
    return switch (qualified(type, imports)) {
      case "com.google.gwt.dom.client.Element", "com.google.gwt.core.client.JavaScriptObject" ->
          "(" + value + " == null ? null : " + value + ".unwrap())";
      case "void", "boolean", "byte", "short", "int", "long", "float", "double", "char", "String" ->
          value;
      default -> "jsinterop.base.Js.asAny(" + value + ")";
    };
  }
}
