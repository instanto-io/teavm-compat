package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import java.util.*;

/** Bridges GWT DOM wrappers at native method boundaries while retaining the public Java API. */
public final class GwtNativeBoundary {
  private static final String TEAVM_BODY = "JSBody";
  private static final String TEAVM_PROPERTY = "JSProperty";
  private static final String TEAVM_METHOD = "JSMethod";
  private static final String JAVA_OBJECT = "Object";

  private final Set<String> overlays;

  private GwtNativeBoundary(Set<String> overlays) {
    this.overlays = overlays;
  }

  static void adapt(CompilationUnit cu) {
    adapt(cu, Set.of());
  }

  static void adapt(CompilationUnit cu, Set<String> overlays) {
    new GwtNativeBoundary(overlays).transform(cu);
  }

  private void transform(CompilationUnit cu) {
    Map<String, String> imported = nativeImports(cu);
    for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      boolean nativeOwner = owner.isAnnotationPresent("JSClass");
      boolean nativeInterface =
          owner.isInterface()
              && !owner.isAnnotationPresent("JSFunctor")
              && owner.getExtendedTypes().stream()
                  .anyMatch(t -> t.getNameAsString().equals("JSObject"));
      if (nativeOwner) adaptFieldGetters(owner, imported);
      int index = 0;
      for (var method : new ArrayList<>(owner.getMethods())) {
        if (nativeInterface && method.getBody().isEmpty() && isErasedResult(method)) {
          method.setNative(true);
          adaptMethod(owner, method, true, index++, imported);
        } else if (needsBoundary(method, nativeOwner, imported))
          adaptMethod(owner, method, nativeOwner, index++, imported);
      }
    }
  }

  private Map<String, String> nativeImports(CompilationUnit cu) {
    Map<String, String> imported = new HashMap<>();
    String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString() + ".").orElse("");
    for (String overlay : overlays) {
      if (overlay.substring(0, overlay.lastIndexOf('.') + 1).equals(pkg))
        imported.put(overlay.substring(overlay.lastIndexOf('.') + 1), overlay);
    }
    cu.getImports().forEach(i -> imported.put(i.getName().getIdentifier(), i.getNameAsString()));
    return imported;
  }

  private void adaptFieldGetters(ClassOrInterfaceDeclaration owner, Map<String, String> imported) {
    // Overlay accessors for native DOM fields need the same wrapper boundary as methods.
    for (var field : owner.getFields()) {
      for (var variable : field.getVariables()) {
        if (isGwt(variable.getTypeAsString(), imported)) adaptFieldGetter(owner, variable);
      }
    }
  }

  private static void adaptFieldGetter(
      ClassOrInterfaceDeclaration owner, VariableDeclarator variable) {
    for (var getter : owner.getMethods()) {
      if (!returnsField(getter, variable)) continue;
      getter.removeBody();
      getter.setNative(true);
      getter.addAnnotation(
          StaticJavaParser.parseAnnotation("@JSProperty(\"" + variable.getNameAsString() + "\")"));
    }
  }

  private static boolean returnsField(MethodDeclaration getter, VariableDeclarator variable) {
    if (!getter.getParameters().isEmpty()
        || getter.getBody().isEmpty()
        || !getter.getTypeAsString().equals(variable.getTypeAsString())) return false;
    var statements = getter.getBody().orElseThrow().getStatements();
    if (statements.size() != 1 || !statements.get(0).isReturnStmt()) return false;
    var expression = statements.get(0).asReturnStmt().getExpression().orElse(null);
    String name = variable.getNameAsString();
    return expression != null
        && (expression.toString().equals(name) || expression.toString().equals("this." + name));
  }

  private boolean needsBoundary(
      MethodDeclaration method, boolean nativeOwner, Map<String, String> imported) {
    if (!method.isNative()) return false;
    boolean body = method.isAnnotationPresent(TEAVM_BODY);
    if (!nativeOwner && !body) return false;
    boolean property = method.isAnnotationPresent(TEAVM_PROPERTY);
    boolean result =
        isGwt(method.getTypeAsString(), imported)
            || isErasedResult(method)
            || (method.getTypeAsString().equals(JAVA_OBJECT) && !property)
            || (body && !nativeOwner && !method.isStatic());
    return result
        || method.getParameters().stream()
            .anyMatch(p -> isGwt(p.getTypeAsString(), imported) || (isObjectValue(p) && !property));
  }

  private void adaptMethod(
      ClassOrInterfaceDeclaration owner,
      MethodDeclaration method,
      boolean nativeOwner,
      int index,
      Map<String, String> imported) {
    MethodDeclaration bridge = createBridge(owner, method, nativeOwner, index);
    StringBuilder setup = new StringBuilder();
    List<String> args = adaptArguments(method, bridge, imported, setup);
    String call = bridge.getNameAsString() + "(" + String.join(",", args) + ")";
    if (owner.isInterface()) {
      String helper = "$NativeResult" + index;
      String variables =
          owner.getTypeParameters().isEmpty()
              ? ""
              : "<"
                  + String.join(
                      ",",
                      owner.getTypeParameters().stream().map(p -> p.getNameAsString()).toList())
                  + ">";
      call = "jsinterop.base.Js.<" + helper + variables + ">uncheckedCast(this)." + call;
    }
    appendResult(method, bridge, call, setup, imported);
    if (owner.isInterface()) {
      var helper =
          new ClassOrInterfaceDeclaration()
              .setInterface(true)
              .setPublic(true)
              .setName("$NativeResult" + index)
              .addExtendedType("org.teavm.jso.JSObject");
      owner.getTypeParameters().forEach(p -> helper.addTypeParameter(p.clone()));
      helper.addMember(bridge);
      owner.addMember(helper);
    } else owner.addMember(bridge);
    method.setNative(false);
    if (owner.isInterface()) method.setDefault(true).setAbstract(false);
    method
        .getAnnotations()
        .removeIf(
            a ->
                Set.of(TEAVM_METHOD, TEAVM_PROPERTY, "JSTopLevel", TEAVM_BODY)
                    .contains(a.getNameAsString()));
    method.setBody(StaticJavaParser.parseBlock("{" + setup + "}"));
  }

  private static MethodDeclaration createBridge(
      ClassOrInterfaceDeclaration owner, MethodDeclaration method, boolean nativeOwner, int index) {
    boolean body = method.isAnnotationPresent(TEAVM_BODY);
    MethodDeclaration bridge = method.clone();
    bridge.setName("$gwtBridge" + index).setPublic(false).setProtected(false).setPrivate(true);
    if (owner.isInterface()) {
      bridge.setName("$gwtBridge" + owner.getNameAsString() + index);
      bridge
          .setPrivate(false)
          .setPublic(true)
          .setNative(false)
          .setAbstract(false)
          .setDefault(false);
    }
    String jsName = method.getNameAsString();
    var annotation = method.getAnnotationByName(TEAVM_METHOD);
    if (annotation.isPresent() && annotation.get().isSingleMemberAnnotationExpr())
      jsName =
          annotation
              .get()
              .asSingleMemberAnnotationExpr()
              .getMemberValue()
              .asStringLiteralExpr()
              .asString();
    bridge.getAnnotations().removeIf(a -> a.getNameAsString().equals(TEAVM_METHOD));
    var property = bridge.getAnnotationByName(TEAVM_PROPERTY);
    if (property.isPresent() && property.get().isMarkerAnnotationExpr()) {
      String original = method.getNameAsString();
      String stem =
          original.startsWith("is")
              ? original.substring(2)
              : original.startsWith("get") || original.startsWith("set")
                  ? original.substring(3)
                  : "";
      if (stem.isEmpty())
        throw new IllegalArgumentException("Cannot infer native property: " + original);
      String name = Character.toLowerCase(stem.charAt(0)) + stem.substring(1);
      property.get().remove();
      bridge.addAnnotation(StaticJavaParser.parseAnnotation("@JSProperty(\"" + name + "\")"));
    }
    if (!body && bridge.getAnnotationByName(TEAVM_PROPERTY).isEmpty())
      bridge.addAnnotation(StaticJavaParser.parseAnnotation("@JSMethod(\"" + jsName + "\")"));
    if (body && !nativeOwner) {
      requireExplicitReceiver(owner, method);
      bridge.setStatic(true);
    }
    return bridge;
  }

  private static void requireExplicitReceiver(
      ClassOrInterfaceDeclaration owner, MethodDeclaration method) {
    var jsBody = method.getAnnotationByName(TEAVM_BODY).orElseThrow().asNormalAnnotationExpr();
    for (var pair : jsBody.getPairs()) {
      if (pair.getNameAsString().equals("script")
          && pair.getValue().asStringLiteralExpr().asString().matches("(?s).*\\bthis\\b.*"))
        throw new IllegalArgumentException(
            "JSNI receiver needs an explicit bridge: "
                + owner.getNameAsString()
                + "."
                + method.getNameAsString());
    }
  }

  private List<String> adaptArguments(
      MethodDeclaration method,
      MethodDeclaration bridge,
      Map<String, String> imported,
      StringBuilder setup) {
    List<String> args = new ArrayList<>();
    for (int i = 0; i < method.getParameters().size(); i++) {
      Parameter parameter = method.getParameter(i);
      String name = parameter.getNameAsString();
      if (isGwt(parameter.getTypeAsString(), imported)) {
        String rawType = nativeType(parameter.getTypeAsString(), imported);
        bridge.getParameter(i).setType(rawType);
        args.add(unwrapArgument(parameter, rawType, i, setup));
      } else if (isObjectValue(parameter) && !method.isAnnotationPresent(TEAVM_PROPERTY)) {
        bridge.getParameter(i).setType("jsinterop.base.Any");
        args.add("jsinterop.base.Js.asAny(" + name + ")");
      } else args.add(name);
    }
    return args;
  }

  private static String unwrapArgument(
      Parameter parameter, String rawType, int i, StringBuilder setup) {
    String n = parameter.getNameAsString();
    if (!parameter.isVarArgs() && !parameter.getType().isArrayType())
      return n + " == null ? null : " + n + ".unwrap()";
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
    return "$raw" + i;
  }

  private void appendResult(
      MethodDeclaration method,
      MethodDeclaration bridge,
      String call,
      StringBuilder setup,
      Map<String, String> imported) {
    if (isGwt(method.getTypeAsString(), imported)) {
      String type = method.getTypeAsString();
      bridge.setType(nativeType(type, imported));
      if (method.getType().isArrayType()) {
        appendArrayResult(type, bridge.getTypeAsString(), call, setup, imported);
      } else setup.append("return ").append(wrap(type, call, imported)).append(";");
    } else if (method.getTypeAsString().equals(JAVA_OBJECT) || isErasedResult(method)) {
      if (isErasedResult(method)) bridge.setType(JAVA_OBJECT);
      setup.append("return jsinterop.base.Js.cast(").append(call).append(");");
    } else setup.append(method.getType().isVoidType() ? "" : "return ").append(call).append(";");
  }

  /** An unbounded native type variable has the same undefined/null boundary as Object. */
  static boolean isErasedResult(MethodDeclaration method) {
    String result = method.getTypeAsString();
    var parameters = new ArrayList<>(method.getTypeParameters());
    method
        .findAncestor(ClassOrInterfaceDeclaration.class)
        .ifPresent(owner -> parameters.addAll(owner.getTypeParameters()));
    return parameters.stream()
        .anyMatch(
            p ->
                p.getNameAsString().equals(result)
                    && (p.getTypeBound().isEmpty()
                        || (p.getTypeBound().size() == 1
                            && Set.of("Object", "java.lang.Object")
                                .contains(p.getTypeBound().get(0).asString()))));
  }

  private void appendArrayResult(
      String type, String rawType, String call, StringBuilder setup, Map<String, String> imported) {
    String base = type.replace("[]", "");
    setup
        .append(rawType)
        .append(" $result = ")
        .append(call)
        .append("; if($result==null) return null;")
        .append(type)
        .append(" $wrapped = new ")
        .append(base)
        .append("[$result.length]; for(int j=0;j<$result.length;j++) $wrapped[j]=")
        .append(wrap(base, "$result[j]", imported))
        .append(";return $wrapped;");
  }

  private static boolean isObjectValue(Parameter parameter) {
    return parameter.getTypeAsString().equals(JAVA_OBJECT) && !parameter.isVarArgs();
  }

  private static String qualified(String type, Map<String, String> imports) {
    String base = type.replace("[]", "");
    return imports.getOrDefault(base, base);
  }

  private boolean isGwt(String type, Map<String, String> imports) {
    return overlays.contains(qualified(type, imports))
        || Set.of(
                "com.google.gwt.dom.client.Element",
                "com.google.gwt.dom.client.Node",
                "com.google.gwt.core.client.JavaScriptObject",
                "com.google.gwt.core.client.JsDate",
                "com.google.gwt.dom.client.NativeEvent",
                "com.google.gwt.user.client.Event",
                "com.google.gwt.user.client.Element",
                "com.google.gwt.core.client.JsArrayNumber",
                "com.google.gwt.core.client.JsArrayString",
                "com.google.gwt.core.client.JsArrayMixed")
            .contains(qualified(type, imports));
  }

  private static String nativeType(String type, Map<String, String> imports) {
    String base =
        switch (qualified(type, imports)) {
          case "com.google.gwt.dom.client.Element", "com.google.gwt.user.client.Element" ->
              "org.teavm.jso.dom.html.HTMLElement";
          case "com.google.gwt.dom.client.Node" -> "org.teavm.jso.dom.xml.Node";
          case "com.google.gwt.dom.client.NativeEvent", "com.google.gwt.user.client.Event" ->
              "org.teavm.jso.dom.events.Event";
          default -> "org.teavm.jso.JSObject";
        };
    return base + (type.endsWith("[]") ? "[]" : "");
  }

  private String wrap(String type, String value, Map<String, String> imports) {
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
      case "com.google.gwt.dom.client.NativeEvent" ->
          "com.google.gwt.dom.client.NativeEvent.wrap(" + value + ")";
      case "com.google.gwt.user.client.Event" ->
          "com.google.gwt.user.client.Event.as(com.google.gwt.dom.client.NativeEvent.wrap("
              + value
              + "))";
      case "com.google.gwt.user.client.Element" ->
          "com.google.gwt.user.client.Element.as(com.google.gwt.dom.client.Element.as("
              + "com.google.gwt.core.client.JavaScriptObject.of("
              + value
              + ")))";
      case "com.google.gwt.core.client.JsArrayNumber",
          "com.google.gwt.core.client.JsArrayString",
          "com.google.gwt.core.client.JsArrayMixed" ->
          qualified(type, imports) + ".of(" + value + ")";
      default -> {
        if (!overlays.contains(qualified(type, imports)))
          throw new IllegalArgumentException("Unsupported native GWT return: " + type);
        yield qualified(type, imports) + ".$wrapNative(" + value + ")";
      }
    };
  }
}
