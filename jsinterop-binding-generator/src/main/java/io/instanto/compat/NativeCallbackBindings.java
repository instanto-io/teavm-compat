package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.*;
import java.util.*;

/** Adapts generic JsFunction arguments using their original functional declarations. */
public final class NativeCallbackBindings {
  private static final String TEAVM_PROPERTY = "JSProperty";

  private record Function(
      List<String> variables,
      List<String> bounds,
      String method,
      List<String> args,
      String result) {}

  private final Map<String, ClassOrInterfaceDeclaration> declarationsByName = new LinkedHashMap<>();

  private final Map<String, Function> functions = new LinkedHashMap<>();

  public NativeCallbackBindings(Collection<String> declarations) {
    for (String source : declarations)
      for (var type : StaticJavaParser.parse(source).findAll(ClassOrInterfaceDeclaration.class))
        type.getFullyQualifiedName().ifPresent(name -> declarationsByName.put(name, type));
    for (String source : declarations) {
      var cu = StaticJavaParser.parse(source);
      Map<String, String> declarationImports = new HashMap<>();
      cu.getImports().stream()
          .filter(i -> !i.isAsterisk())
          .forEach(i -> declarationImports.put(i.getName().getIdentifier(), i.getNameAsString()));
      for (var type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
        type.getFullyQualifiedName().ifPresent(name -> declarationsByName.put(name, type));
        if (type.getAnnotationByName("JsFunction").isEmpty()) continue;
        registerFunction(type, declarationImports);
      }
    }
  }

  private void registerFunction(
      ClassOrInterfaceDeclaration type, Map<String, String> declarationImports) {
    var methods =
        type.getMethods().stream().filter(m -> !m.isStatic() && m.getBody().isEmpty()).toList();
    if (methods.size() != 1)
      throw new IllegalArgumentException("Expected single JsFunction method: " + type.getName());
    var method = methods.get(0);
    String name = type.getFullyQualifiedName().orElseThrow();
    functions.put(
        name,
        new Function(
            type.getTypeParameters().stream().map(t -> t.getNameAsString()).toList(),
            type.getTypeParameters().stream()
                .map(
                    t ->
                        t.getTypeBound().isEmpty()
                            ? "Object"
                            : boundName(
                                t.getTypeBound().get(0).getNameWithScope(),
                                type,
                                declarationImports))
                .toList(),
            method.getNameAsString(),
            method.getParameters().stream()
                .map(p -> qualified(p.getTypeAsString(), declarationImports))
                .toList(),
            qualified(method.getTypeAsString(), declarationImports)));
  }

  private String boundName(
      String name, ClassOrInterfaceDeclaration owner, Map<String, String> imports) {
    String imported = qualified(name, imports);
    if (!imported.equals(name) || name.contains(".")) return imported;
    String scope = owner.getFullyQualifiedName().orElse("");
    while (!scope.isEmpty()) {
      if (declarationsByName.containsKey(scope + "." + name)) return scope + "." + name;
      int dot = scope.lastIndexOf('.');
      scope = dot < 0 ? "" : scope.substring(0, dot);
    }
    return name;
  }

  public String transform(String source) {
    var cu = StaticJavaParser.parse(source);
    Map<String, String> imports = new HashMap<>();
    cu.getImports().forEach(i -> imports.put(i.getName().getIdentifier(), i.getNameAsString()));
    boolean changed = false;
    for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      imports.put("$scope", owner.getFullyQualifiedName().orElse(""));
      int next = 0;
      for (var method : new ArrayList<>(owner.getMethods())) {
        if (!method.isNative() && !(owner.isInterface() && method.getBody().isEmpty())) continue;
        List<Shape> shapes = new ArrayList<>();
        for (var p : method.getParameters()) shapes.add(shape(p.getType(), imports));
        Shape result = shape(method.getType(), imports);
        if (result == null && shapes.stream().allMatch(Objects::isNull)) continue;
        if (!method.isStatic() && inheritedAdapter(owner, method, new HashSet<>())) {
          method.remove();
          changed = true;
          continue;
        }
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
        if (method.getAnnotationByName("JSBody").isPresent()) {
          bridge.addAnnotation(method.getAnnotationByName("JSBody").orElseThrow().clone());
        } else if (method.getAnnotationByName(TEAVM_PROPERTY).isPresent()) {
          bridge.addAnnotation(method.getAnnotationByName(TEAVM_PROPERTY).orElseThrow().clone());
        } else {
          bridge.addAnnotation(
              StaticJavaParser.parseAnnotation("@org.teavm.jso.JSMethod(\"" + nativeName + "\")"));
        }
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
        String helper = "$Native" + suffix;
        String ownerArguments =
            owner.getTypeParameters().isEmpty()
                ? ""
                : "<"
                    + String.join(
                        ",",
                        owner.getTypeParameters().stream().map(t -> t.getNameAsString()).toList())
                    + ">";
        String receiver =
            owner.isInterface()
                ? "((" + helper + ownerArguments + ")(org.teavm.jso.JSObject)this)."
                : "";
        String call = receiver + bridge.getNameAsString() + "(" + String.join(",", args) + ")";
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
        if (owner.isInterface()) {
          ClassOrInterfaceDeclaration nativeView =
              new ClassOrInterfaceDeclaration().setName(helper).setInterface(true).setPublic(true);
          nativeView.addExtendedType("org.teavm.jso.JSObject");
          for (var parameter : owner.getTypeParameters())
            nativeView.getTypeParameters().add(parameter.clone());
          bridge.setNative(false).setPublic(true);
          nativeView.addMember(bridge);
          owner.addMember(nativeView);
        } else owner.addMember(bridge);
        method.setNative(false);
        method
            .getAnnotations()
            .removeIf(
                a ->
                    Set.of("JSMethod", TEAVM_PROPERTY, "JSBody")
                        .contains(a.getName().getIdentifier()));
        if (owner.isInterface()) method.setDefault(true);
        method.setBody(StaticJavaParser.parseBlock(body));
        changed = true;
      }
    }
    return changed ? cu.toString() : source;
  }

  /** TeaVM overlay methods with Java bodies cannot override an inherited overlay method. */
  private boolean inheritedAdapter(
      ClassOrInterfaceDeclaration owner, MethodDeclaration method, Set<String> seen) {
    List<ClassOrInterfaceType> parents = new ArrayList<>(owner.getExtendedTypes());
    parents.addAll(owner.getImplementedTypes());
    var unit = owner.findCompilationUnit().orElseThrow();
    String pkg = unit.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("");
    Map<String, String> imports = new HashMap<>();
    unit.getImports().stream()
        .filter(i -> !i.isAsterisk())
        .forEach(i -> imports.put(i.getName().getIdentifier(), i.getNameAsString()));
    for (var parent : parents) {
      String name =
          imports.getOrDefault(parent.getNameWithScope(), pkg + parent.getNameWithScope());
      if (!declarationsByName.containsKey(name)) name = parent.getNameWithScope();
      var declaration = declarationsByName.get(name);
      if (declaration == null || !seen.add(name)) continue;
      for (var candidate : declaration.getMethods()) {
        if (!candidate.isStatic()
            && candidate.getBody().isEmpty()
            && candidate.getSignature().equals(method.getSignature())
            && candidate.getTypeAsString().equals(method.getTypeAsString())
            && candidate
                .getAnnotationByName("JsMethod")
                .map(Object::toString)
                .equals(method.getAnnotationByName("JsMethod").map(Object::toString))) return true;
      }
      if (inheritedAdapter(declaration, method, seen)) return true;
    }
    return false;
  }

  private record Shape(Function function, List<String> args, String result, String key) {}

  private Shape shape(Type type, Map<String, String> imports) {
    if (!type.isClassOrInterfaceType()) return null;
    var declared = type.asClassOrInterfaceType();
    String name = resolveFunctionName(declared, imports);
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
          parameters.isEmpty() ? function.bounds.get(i) : parameters.get(i).toString());
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

  private String resolveFunctionName(ClassOrInterfaceType declared, Map<String, String> imports) {
    String declaredName = declared.getNameWithScope();
    int dot = declaredName.indexOf('.');
    String first = dot < 0 ? declaredName : declaredName.substring(0, dot);
    String resolved =
        imports.getOrDefault(first, first) + (dot < 0 ? "" : declaredName.substring(dot));
    if (!imports.containsKey(first)) {
      String scope = imports.getOrDefault("$scope", "");
      while (!scope.isEmpty()) {
        if (functions.containsKey(scope + "." + declaredName)) {
          resolved = scope + "." + declaredName;
          break;
        }
        int parent = scope.lastIndexOf('.');
        scope = parent < 0 ? "" : scope.substring(0, parent);
      }
    }
    return resolved;
  }

  private static boolean needsBridge(String type, Map<String, String> imports) {
    return Set.of(
            "Object",
            "java.lang.Object",
            "Double",
            "Integer",
            "Boolean",
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.Boolean",
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
      default ->
          (!type.startsWith("?") ? "(" + type + ") " : "")
              + "jsinterop.base.Js.cast("
              + value
              + ")";
    };
  }

  private static String outbound(String type, String value, Map<String, String> imports) {
    return switch (qualified(type, imports)) {
      case "com.google.gwt.dom.client.Element", "com.google.gwt.core.client.JavaScriptObject" ->
          "com.google.gwt.core.client.JavaScriptObject.unwrapNullable(" + value + ")";
      case "void", "boolean", "byte", "short", "int", "long", "float", "double", "char", "String" ->
          value;
      default -> "jsinterop.base.Js.asAny(" + value + ")";
    };
  }
}
