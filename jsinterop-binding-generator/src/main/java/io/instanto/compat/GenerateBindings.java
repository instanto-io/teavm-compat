package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Deterministic JsInterop declaration to TeaVM JSO source adapter. */
public final class GenerateBindings {
  private static final String GLOBAL_NAMESPACE = "<global>";
  private static final String GLOBAL_CONSTANT = "GLOBAL";
  private static final String QUALIFIED_GLOBAL_CONSTANT = "JsPackage.GLOBAL";
  private static final String JS_INTEROP_METHOD = "JsMethod";
  private static final String JS_INTEROP_PROPERTY = "JsProperty";
  private static final String TEAVM_METHOD = "JSMethod";
  private static final String TEAVM_BODY = "JSBody";

  private static String value(AnnotationExpr a, String key, String fallback) {
    if (a instanceof NormalAnnotationExpr normal) {
      return normal.getPairs().stream()
          .filter(p -> p.getNameAsString().equals(key))
          .map(
              p ->
                  p.getValue().isStringLiteralExpr()
                      ? p.getValue().asStringLiteralExpr().asString()
                      : p.getValue().toString())
          .findFirst()
          .orElse(fallback);
    }
    return fallback;
  }

  private static void annotate(NodeWithAnnotations<?> n, String text) {
    n.addAnnotation(StaticJavaParser.parseAnnotation(text));
  }

  public static String transform(String source) {
    return transform(source, Set.of());
  }

  static String transform(String source, Set<String> overlays) {
    CompilationUnit cu = StaticJavaParser.parse(source);
    boolean eventFactory =
        source.contains("com.google.gwt.event.dom.client.DomEvent") && adaptEventFactories(cu);
    if (!source.contains("jsinterop.annotations")) {
      if (!source.contains("@JSBody")) return eventFactory ? cu.toString() : source;
      GwtNativeBoundary.adapt(cu, overlays);
      return cu.toString();
    }
    cu.addImport("org.teavm.jso.*");
    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(GenerateBindings::adaptNativeType);
    cu.findAll(MethodCallExpr.class).forEach(GenerateBindings::adaptUnionCast);
    new ArrayList<>(cu.findAll(AnnotationExpr.class)).forEach(GenerateBindings::adaptAnnotation);
    normalizePromiseCallbacks(cu);
    GwtNativeBoundary.adapt(cu, overlays);
    adaptObjectVarargs(cu);
    return cu.toString();
  }

  private static boolean adaptEventFactories(CompilationUnit cu) {
    boolean eventFactory = false;

    for (ClassOrInterfaceDeclaration owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      if (owner.getExtendedTypes().stream().noneMatch(t -> t.getNameAsString().equals("DomEvent")))
        continue;
      for (ObjectCreationExpr creation : owner.findAll(ObjectCreationExpr.class)) {
        if (creation.getType().getNameAsString().equals("Type")
            && creation.getArguments().size() == 2
            && creation.getArgument(1).isObjectCreationExpr()) {
          creation.setArgument(
              1, StaticJavaParser.parseExpression("() -> " + creation.getArgument(1)));
          eventFactory = true;
        }
      }
    }

    return eventFactory;
  }

  private static void adaptNativeType(ClassOrInterfaceDeclaration c) {
    var jsType = c.getAnnotationByName("JsType");
    boolean functor = c.getAnnotationByName("JsFunction").isPresent();
    if (jsType.isEmpty() && !functor) return;
    if (jsType.isPresent() && !value(jsType.get(), "isNative", "false").equals("true")) return;
    String name =
        jsType.map(a -> value(a, "name", c.getNameAsString())).orElse(c.getNameAsString());
    String namespace = jsType.map(a -> value(a, "namespace", "")).orElse("");
    boolean global = name.equals("goog.global");
    addStandardConstructors(c);
    if (c.isInterface()) c.addExtendedType("JSObject");
    else {
      c.addImplementedType("JSObject");
      String qualified =
          global
              ? "globalThis"
              : "globalThis."
                  + ((!namespace.isEmpty()
                          && !Set.of(QUALIFIED_GLOBAL_CONSTANT, GLOBAL_CONSTANT, GLOBAL_NAMESPACE)
                              .contains(namespace))
                      ? namespace + "."
                      : "")
                  + name;
      // Explicit global access also prevents minifier names (e.g. CSS) shadowing browser globals.
      annotate(c, "@JSClass(name = \"" + qualified + "\")");
    }
    if (functor) annotate(c, "@JSFunctor");
    if (!functor) annotateImplicitMethods(c);
    removeOverlayCasts(c);
    adaptNativeFields(c);
  }

  private static void addStandardConstructors(ClassOrInterfaceDeclaration c) {
    if (c.getNameAsString().equals("JsDate") && !c.isInterface()) {
      for (int count = 1; count <= 7; count++) {
        var ctor = c.addConstructor(Modifier.Keyword.PUBLIC);
        for (int i = 0; i < count; i++) ctor.addParameter("double", "value" + i);
      }
      c.addConstructor(Modifier.Keyword.PUBLIC).addParameter("String", "value");
    }
    if (c.getNameAsString().equals("JsArray") && !c.isInterface()) {
      // TeaVM 0.15 spreads varargs methods, but not native constructors.
      // Preserve correct zero-argument Array construction used by Intl locale lists.
      c.addConstructor(Modifier.Keyword.PUBLIC).setBody(StaticJavaParser.parseBlock("{}"));
    }
  }

  private static void annotateImplicitMethods(ClassOrInterfaceDeclaration c) {
    // JsType makes native methods implicit JS methods. TeaVM needs an explicit annotation
    // when the same declaration is inherited through both a class and an interface.
    for (MethodDeclaration method : c.getMethods()) {
      if ((method.isNative() || c.isInterface() && method.getBody().isEmpty())
          && method.getAnnotationByName(JS_INTEROP_METHOD).isEmpty()
          && method.getAnnotationByName(JS_INTEROP_PROPERTY).isEmpty()
          && method.getAnnotationByName(TEAVM_METHOD).isEmpty()
          && method.getAnnotationByName("JSProperty").isEmpty()
          && method.getAnnotationByName(TEAVM_BODY).isEmpty()) annotate(method, "@JSMethod");
    }
  }

  private static void removeOverlayCasts(ClassOrInterfaceDeclaration c) {
    for (MethodDeclaration m : new ArrayList<>(c.getMethodsByName("cast"))) {
      if (m.getAnnotationByName("JsOverlay").isPresent()
          && m.getParameters().isEmpty()
          && m.getTypeParameters().size() == 1
          && m.getBody()
              .map(Object::toString)
              .orElse("")
              .replaceAll("\\s", "")
              .equals("{return(T)this;}")) m.remove();
    }
  }

  private static void adaptNativeFields(ClassOrInterfaceDeclaration c) {
    for (FieldDeclaration f : c.getFields()) {
      if (f.getAnnotationByName("JsOverlay").isPresent()) continue;
      if (f.isStatic() && f.getAnnotationByName(JS_INTEROP_PROPERTY).isEmpty())
        annotate(f, "@JSProperty");
      for (var v : f.getVariables()) v.removeInitializer();
    }
  }

  private static void adaptUnionCast(MethodCallExpr call) {
    if (!call.getScope().map(Object::toString).orElse("").equals("Js")
        || !Set.of("cast", "uncheckedCast").contains(call.getNameAsString())
        || !call.getTypeArguments().map(Object::toString).orElse("").contains("UnionType")) return;
    call.setName("nativeCast");
    // Preserve the declared functor type before a union erases it to Object.
    var method = call.findAncestor(MethodDeclaration.class);
    if (method.isEmpty() || !call.getArgument(0).isNameExpr()) return;
    String arg = call.getArgument(0).asNameExpr().getNameAsString();
    var parameter =
        method.get().getParameters().stream()
            .filter(p -> p.getNameAsString().equals(arg))
            .findFirst();
    if (parameter.isEmpty()) return;
    String type = parameter.get().getType().toString().replaceAll("<.*>", "");
    if (!type.endsWith("Fn")) return;
    var owner = method.get().findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow();
    String helper = "$wrap" + type.replace('.', '_');
    addFunctorWrapper(owner, helper, type);
    call.setArgument(0, new MethodCallExpr(helper).addArgument(arg));
  }

  private static void addFunctorWrapper(
      ClassOrInterfaceDeclaration owner, String helper, String type) {
    if (!owner.getMethodsByName(helper).isEmpty()) return;
    var bridge =
        owner.addMethod(
            helper, Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE);
    bridge.setType("JSObject").addParameter(type, "value").removeBody();
    annotate(bridge, "@JSBody(params=\"value\",script=\"return value;\")");
  }

  private static void adaptAnnotation(AnnotationExpr annotation) {
    String kind = annotation.getNameAsString();
    if (kind.equals(JS_INTEROP_PROPERTY) || kind.equals(JS_INTEROP_METHOD)) {
      adaptMemberAnnotation(annotation, kind);
    }
    if (Set.of(
            "JsType",
            "JsFunction",
            "JsOverlay",
            JS_INTEROP_PROPERTY,
            JS_INTEROP_METHOD,
            "JsConstructor")
        .contains(kind)) annotation.remove();
  }

  private static void adaptMemberAnnotation(AnnotationExpr annotation, String kind) {
    var owner = (NodeWithAnnotations<?>) annotation.getParentNode().orElseThrow();
    String name = value(annotation, "name", "");
    String namespace = value(annotation, "namespace", "");
    boolean global =
        Set.of(QUALIFIED_GLOBAL_CONSTANT, GLOBAL_CONSTANT, GLOBAL_NAMESPACE).contains(namespace);
    if (kind.equals(JS_INTEROP_METHOD)
        && !namespace.isEmpty()
        && !global
        && owner instanceof MethodDeclaration method
        && method.isStatic()) {
      addNamespaceBody(method, namespace, name.isEmpty() ? method.getNameAsString() : name);
      return;
    }
    if (kind.equals(JS_INTEROP_METHOD) && global) annotate(owner, "@JSTopLevel");
    annotate(
        owner,
        "@"
            + (kind.equals(JS_INTEROP_PROPERTY) ? "JSProperty" : TEAVM_METHOD)
            + (name.isEmpty() ? "" : "(\"" + name + "\")"));
  }

  private static void addNamespaceBody(MethodDeclaration method, String ns, String member) {
    NodeList<Expression> parameters = new NodeList<>();
    List<String> arguments = new ArrayList<>();
    String variableArguments = null;
    for (Parameter parameter : method.getParameters()) {
      // Java permits names such as "function", which are reserved in JavaScript.
      String argument = "arg" + parameters.size();
      parameters.add(new StringLiteralExpr(argument));
      if (parameter.isVarArgs()) variableArguments = argument;
      else arguments.add(argument);
    }
    String receiver = "globalThis." + ns;
    String invocation = receiver + "[" + new StringLiteralExpr(member) + "]";
    if (variableArguments == null) {
      invocation += "(" + String.join(",", arguments) + ")";
    } else {
      // JSBody uses TeaVM's JavaScript parser; avoid unsupported spread syntax.
      invocation +=
          ".apply("
              + receiver
              + ",["
              + String.join(",", arguments)
              + "].concat("
              + variableArguments
              + "))";
    }
    String script = (method.getType().isVoidType() ? "" : "return ") + invocation + ";";
    method.addAnnotation(
        new NormalAnnotationExpr(
            new Name(TEAVM_BODY),
            NodeList.nodeList(
                new MemberValuePair("params", new ArrayInitializerExpr(parameters)),
                new MemberValuePair("script", new StringLiteralExpr().setString(script)))));
  }

  private static void normalizePromiseCallbacks(CompilationUnit cu) {
    // Normalize erased promise arguments before application bridges cast to String/Number.
    if (!cu.getPackageDeclaration()
        .map(p -> p.getNameAsString())
        .orElse("")
        .equals("elemental2.promise")) return;
    for (var owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      for (var method : new ArrayList<>(owner.getMethods())) {
        if (Set.of("then", "catch_").contains(method.getNameAsString())
            && method.getBody().isEmpty()) adaptPromiseMethod(owner, method);
      }
    }
  }

  private static void adaptPromiseMethod(ClassOrInterfaceDeclaration owner, MethodDeclaration m) {
    String nativeName = m.getNameAsString().equals("catch_") ? "catch" : "then";
    MethodDeclaration bridge = m.clone();
    bridge.setName("$native" + m.getNameAsString());
    bridge.getAnnotations().clear();
    annotate(bridge, "@JSMethod(\"" + nativeName + "\")");
    owner.addMember(bridge);
    m.getAnnotations().removeIf(a -> a.getNameAsString().equals(TEAVM_METHOD));
    m.setNative(false);
    if (owner.isInterface()) m.setDefault(true);
    List<String> args = new ArrayList<>();
    for (var p : m.getParameters()) {
      String n = p.getNameAsString();
      args.add(
          n
              + " == null ? null : value -> jsinterop.base.Js.<IThenable<V>>uncheckedCast("
              + n
              + ".onInvoke(jsinterop.base.Js.cast(value)))");
    }
    m.setBody(
        StaticJavaParser.parseBlock(
            "{return " + bridge.getNameAsString() + "(" + String.join(",", args) + ");}"));
  }

  private static void adaptObjectVarargs(CompilationUnit cu) {
    int index = 0;
    for (var method : new ArrayList<>(cu.findAll(MethodDeclaration.class))) {
      if (!hasObjectVarargs(method)) continue;
      var last = method.getParameter(method.getParameters().size() - 1);
      var owner = method.findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow();
      if (!owner.isInterface()) adaptVarargsMethod(cu, owner, method, last, index++);
    }
  }

  private static boolean hasObjectVarargs(MethodDeclaration method) {
    if (!method.isNative() || method.getParameters().isEmpty()) return false;
    var last = method.getParameter(method.getParameters().size() - 1);
    return last.isVarArgs() && last.getType().toString().equals("Object");
  }

  private static void adaptVarargsMethod(
      CompilationUnit cu,
      ClassOrInterfaceDeclaration owner,
      MethodDeclaration m,
      Parameter last,
      int varargIndex) {
    String helper = "$nativeVarargs" + varargIndex;
    cu.addImport("jsinterop.base.Js");
    MethodDeclaration bridge = m.clone().setName(helper).setPublic(false).setPrivate(true);
    bridge.getParameter(bridge.getParameters().size() - 1).setType("JSObject");
    String jsName =
        m.getAnnotationByName(TEAVM_METHOD)
            .map(
                a ->
                    a.isSingleMemberAnnotationExpr()
                        ? a.asSingleMemberAnnotationExpr()
                            .getMemberValue()
                            .asStringLiteralExpr()
                            .asString()
                        : m.getNameAsString())
            .orElse(m.getNameAsString());
    bridge.getAnnotations().removeIf(a -> a.getNameAsString().equals(TEAVM_METHOD));
    if (!bridge.isAnnotationPresent(TEAVM_BODY)) annotate(bridge, "@JSMethod(\"" + jsName + "\")");
    owner.addMember(bridge);
    String args = last.getNameAsString();
    String prefix =
        m.getParameters().subList(0, m.getParameters().size() - 1).stream()
            .map(Parameter::getNameAsString)
            .collect(java.util.stream.Collectors.joining(","));
    if (!prefix.isEmpty()) prefix += ",";
    m.setNative(false);
    m.getAnnotations()
        .removeIf(
            a -> Set.of("JSTopLevel", TEAVM_METHOD, TEAVM_BODY).contains(a.getNameAsString()));
    m.setBody(
        StaticJavaParser.parseBlock(
            "{JSObject[] nativeArgs=new JSObject["
                + args
                + ".length];for(int i=0;i<nativeArgs.length;i++) nativeArgs[i]=Js.asAny("
                + args
                + "[i]);"
                + (m.getType().isVoidType() ? "" : "return ")
                + helper
                + "("
                + prefix
                + "nativeArgs);}"));
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2)
      throw new IllegalArgumentException("Usage: GenerateBindings OUTPUT INPUT...");
    Path out = Path.of(args[0]).toAbsolutePath().normalize();
    Files.createDirectories(out);
    Map<Path, String> sources = new LinkedHashMap<>();
    for (int i = 1; i < args.length; i++) {
      Path input = Path.of(args[i]).toAbsolutePath().normalize();
      if (input.startsWith(out) || out.startsWith(input))
        throw new IllegalArgumentException("Inputs and output must be separate");
      if (Files.isDirectory(input)) {
        try (var files = Files.walk(input)) {
          for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList())
            sources.put(out.resolve(input.relativize(file)), Files.readString(file));
        }
      } else {
        try (JarFile jar = new JarFile(input.toFile())) {
          for (var entry :
              jar.stream()
                  .filter(e -> e.getName().endsWith(".java"))
                  .sorted(Comparator.comparing(JarEntry::getName))
                  .toList()) {
            Path destination = out.resolve(entry.getName()).normalize();
            if (!destination.startsWith(out))
              throw new IllegalArgumentException("Invalid source entry: " + entry.getName());
            try (var stream = jar.getInputStream(entry)) {
              sources.put(
                  destination,
                  new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
          }
        }
      }
    }
    BindingPipeline pipeline = new BindingPipeline(sources.values(), false);
    for (var source : sources.entrySet()) {
      Files.createDirectories(source.getKey().getParent());
      Files.writeString(source.getKey(), pipeline.transform(source.getValue()));
    }
    StringBuilder manifest = new StringBuilder();
    var digest = java.security.MessageDigest.getInstance("SHA-256");
    try (var files = Files.walk(out)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList())
        manifest
            .append(java.util.HexFormat.of().formatHex(digest.digest(Files.readAllBytes(file))))
            .append("  ")
            .append(out.relativize(file))
            .append("\n");
    }
    Files.writeString(out.resolve("sources.sha256"), manifest);
  }
}
