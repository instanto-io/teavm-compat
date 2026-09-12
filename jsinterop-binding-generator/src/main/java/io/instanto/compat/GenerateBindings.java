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
    CompilationUnit cu = StaticJavaParser.parse(source);
    if (!source.contains("jsinterop.annotations")) return source;
    cu.addImport("org.teavm.jso.*");
    for (ClassOrInterfaceDeclaration c : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      var jsType = c.getAnnotationByName("JsType");
      boolean functor = c.getAnnotationByName("JsFunction").isPresent();
      if (jsType.isEmpty() && !functor) continue;
      if (jsType.isPresent() && !value(jsType.get(), "isNative", "false").equals("true")) continue;
      String name =
          jsType.map(a -> value(a, "name", c.getNameAsString())).orElse(c.getNameAsString());
      String namespace = jsType.map(a -> value(a, "namespace", "")).orElse("");
      boolean global = name.equals("goog.global");
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
      if (c.isInterface()) c.addExtendedType("JSObject");
      else {
        c.addImplementedType("JSObject");
        String qualified =
            global
                ? "globalThis"
                : "globalThis."
                    + ((!namespace.isEmpty() && !namespace.equals("JsPackage.GLOBAL"))
                        ? namespace + "."
                        : "")
                    + name;
        // Explicit global access also prevents minifier names (e.g. CSS) shadowing browser globals.
        annotate(c, "@JSClass(name = \"" + qualified + "\")");
      }
      if (functor) annotate(c, "@JSFunctor");
      for (FieldDeclaration f : c.getFields()) {
        if (f.getAnnotationByName("JsOverlay").isPresent()) continue;
        if (f.isStatic()) annotate(f, "@JSProperty");
        for (var v : f.getVariables()) v.removeInitializer();
      }
    }
    for (MethodCallExpr call : cu.findAll(MethodCallExpr.class)) {
      if (call.getScope().map(Object::toString).orElse("").equals("Js")
          && Set.of("cast", "uncheckedCast").contains(call.getNameAsString())
          && call.getTypeArguments().map(Object::toString).orElse("").contains("UnionType")) {
        call.setName("nativeCast");
        // Preserve the declared functor type before a union erases it to Object.
        var method = call.findAncestor(MethodDeclaration.class);
        if (method.isPresent() && call.getArgument(0).isNameExpr()) {
          String arg = call.getArgument(0).asNameExpr().getNameAsString();
          var parameter =
              method.get().getParameters().stream()
                  .filter(p -> p.getNameAsString().equals(arg))
                  .findFirst();
          if (parameter.isPresent()) {
            String type = parameter.get().getType().toString().replaceAll("<.*>", "");
            if (type.endsWith("Fn")) {
              var owner =
                  method.get().findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow();
              String helper = "$wrap" + type.replace('.', '_');
              if (owner.getMethodsByName(helper).isEmpty()) {
                var bridge =
                    owner.addMethod(
                        helper,
                        Modifier.Keyword.PRIVATE,
                        Modifier.Keyword.STATIC,
                        Modifier.Keyword.NATIVE);
                bridge.setType("JSObject").addParameter(type, "value").removeBody();
                annotate(bridge, "@JSBody(params=\"value\",script=\"return value;\")");
              }
              call.setArgument(0, new MethodCallExpr(helper).addArgument(arg));
            }
          }
        }
      }
    }
    for (AnnotationExpr a : new ArrayList<>(cu.findAll(AnnotationExpr.class))) {
      String n = a.getNameAsString();
      if (n.equals("JsProperty") || n.equals("JsMethod")) {
        var owner = (NodeWithAnnotations<?>) a.getParentNode().orElseThrow();
        String name = value(a, "name", "");
        annotate(
            owner,
            "@"
                + (n.equals("JsProperty") ? "JSProperty" : "JSMethod")
                + (name.isEmpty() ? "" : "(\"" + name + "\")"));
      }
      if (Set.of("JsType", "JsFunction", "JsOverlay", "JsProperty", "JsMethod", "JsConstructor")
          .contains(n)) a.remove();
    }
    // TeaVM erases generic functor arguments to wrapped JS values. Normalize promise
    // callback values before the application's bridge method casts them to String/Number.
    if (cu.getPackageDeclaration()
        .map(p -> p.getNameAsString())
        .orElse("")
        .equals("elemental2.promise")) {
      for (ClassOrInterfaceDeclaration owner : cu.findAll(ClassOrInterfaceDeclaration.class)) {
        for (MethodDeclaration m : new ArrayList<>(owner.getMethods())) {
          if (!(m.getNameAsString().equals("then") || m.getNameAsString().equals("catch_")))
            continue;
          if (m.getBody().isPresent()) continue;
          String nativeName = m.getNameAsString().equals("catch_") ? "catch" : "then";
          MethodDeclaration bridge = m.clone();
          bridge.setName("$native" + m.getNameAsString());
          bridge.getAnnotations().clear();
          annotate(bridge, "@JSMethod(\"" + nativeName + "\")");
          owner.addMember(bridge);
          m.getAnnotations().removeIf(a -> a.getNameAsString().equals("JSMethod"));
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
      }
    }
    int varargIndex = 0;
    for (MethodDeclaration m : new ArrayList<>(cu.findAll(MethodDeclaration.class))) {
      if (!m.isNative() || m.getParameters().isEmpty()) continue;
      var last = m.getParameter(m.getParameters().size() - 1);
      if (!last.isVarArgs() || !last.getType().toString().equals("Object")) continue;
      var owner = m.findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow();
      if (owner.isInterface()) continue;
      String helper = "$nativeVarargs" + (varargIndex++);
      cu.addImport("jsinterop.base.Js");
      MethodDeclaration bridge = m.clone().setName(helper).setPublic(false).setPrivate(true);
      bridge.getParameter(bridge.getParameters().size() - 1).setType("JSObject");
      String jsName =
          m.getAnnotationByName("JSMethod")
              .map(
                  a ->
                      a.isSingleMemberAnnotationExpr()
                          ? a.asSingleMemberAnnotationExpr()
                              .getMemberValue()
                              .asStringLiteralExpr()
                              .asString()
                          : m.getNameAsString())
              .orElse(m.getNameAsString());
      bridge.getAnnotations().removeIf(a -> a.getNameAsString().equals("JSMethod"));
      annotate(bridge, "@JSMethod(\"" + jsName + "\")");
      owner.addMember(bridge);
      String args = last.getNameAsString();
      String prefix =
          m.getParameters().subList(0, m.getParameters().size() - 1).stream()
              .map(Parameter::getNameAsString)
              .collect(java.util.stream.Collectors.joining(","));
      if (!prefix.isEmpty()) prefix += ",";
      m.setNative(false);
      m.getAnnotations()
          .removeIf(a -> Set.of("JSTopLevel", "JSMethod").contains(a.getNameAsString()));
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
    return cu.toString();
  }

  private static void write(Path p, String source) throws Exception {
    Files.createDirectories(p.getParent());
    Files.writeString(p, transform(source));
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2)
      throw new IllegalArgumentException("Usage: GenerateBindings OUTPUT INPUT...");
    Path out = Path.of(args[0]).toAbsolutePath().normalize();
    Files.createDirectories(out);
    for (int i = 1; i < args.length; i++) {
      Path input = Path.of(args[i]).toAbsolutePath().normalize();
      if (input.startsWith(out) || out.startsWith(input))
        throw new IllegalArgumentException("Inputs and output must be separate");
      if (Files.isDirectory(input)) {
        try (var files = Files.walk(input)) {
          for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList())
            write(out.resolve(input.relativize(file)), Files.readString(file));
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
              write(
                  destination,
                  new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
          }
        }
      }
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
