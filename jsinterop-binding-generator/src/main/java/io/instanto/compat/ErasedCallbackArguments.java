package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import java.util.*;

/** Keeps a JsFunction callable when a native generic setter erases its parameter to Object. */
final class ErasedCallbackArguments {
  private final Map<String, ClassOrInterfaceDeclaration> types = new LinkedHashMap<>();

  ErasedCallbackArguments(Collection<String> sources) {
    for (String source : sources) {
      for (var type : StaticJavaParser.parse(source).findAll(ClassOrInterfaceDeclaration.class)) {
        type.getFullyQualifiedName().ifPresent(name -> types.put(name, type));
      }
    }
  }

  String transform(String source) {
    var cu = StaticJavaParser.parse(source);
    boolean changed = false;
    int sequence = 0;
    for (var call : new ArrayList<>(cu.findAll(MethodCallExpr.class))) {
      if (call.getScope().isEmpty()) continue;
      var owner = call.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
      if (owner == null) continue;
      Type receiver = receiverType(call.getScope().get(), call, owner);
      if (receiver == null || !receiver.isClassOrInterfaceType()) continue;
      var declared = receiver.asClassOrInterfaceType();
      var arguments = declared.getTypeArguments().orElse(new NodeList<>());
      if (arguments.isEmpty()) continue;
      var nativeType = types.get(resolve(declared.getNameWithScope(), owner));
      if (nativeType == null || nativeType.getAnnotationByName("JsType").isEmpty()) continue;
      var annotation = nativeType.getAnnotationByName("JsType").get();
      if (!(annotation instanceof NormalAnnotationExpr normal)
          || normal.getPairs().stream()
              .noneMatch(
                  pair ->
                      pair.getNameAsString().equals("isNative")
                          && pair.getValue().isBooleanLiteralExpr()
                          && pair.getValue().asBooleanLiteralExpr().getValue())) continue;
      if (arguments.size() != nativeType.getTypeParameters().size()) continue;
      List<MethodDeclaration> methods =
          nativeType.getMethodsByName(call.getNameAsString()).stream()
              .filter(
                  m ->
                      m.isNative()
                          && m.getType().isVoidType()
                          && m.getParameters().size() == call.getArguments().size())
              .toList();
      // Do not guess overload resolution. A unique native void setter is unambiguous.
      if (methods.size() != 1) continue;
      var method = methods.get(0);
      boolean adapted = false;
      for (int i = 0; i < method.getParameters().size(); i++) {
        String parameter = method.getParameter(i).getTypeAsString();
        int variable = -1;
        for (int j = 0; j < nativeType.getTypeParameters().size(); j++) {
          if (nativeType.getTypeParameter(j).getNameAsString().equals(parameter)) variable = j;
        }
        if (variable < 0) continue;
        String functionName = resolve(arguments.get(variable).toString(), owner);
        var function = types.get(functionName);
        if (function == null || function.getAnnotationByName("JsFunction").isEmpty()) continue;
        if (!function.getTypeParameters().isEmpty())
          throw new IllegalArgumentException(
              "Parameterized erased callback needs an explicit bridge: " + functionName);
        String helper = "$erasedCallback" + sequence++;
        addBridge(owner, function, functionName, helper);
        call.setArgument(i, new MethodCallExpr(helper).addArgument(call.getArgument(i).clone()));
        adapted = true;
      }
      if (adapted) {
        // The helper produces a native JS function. Pass it as Object to the erased native setter,
        // avoiding an intermediate cast back to the Java functor representation.
        var raw = declared.clone();
        raw.removeTypeArguments();
        call.setScope(new EnclosedExpr(new CastExpr(raw, call.getScope().get().clone())));
        changed = true;
      }
    }
    return changed ? cu.toString() : source;
  }

  private void addBridge(
      ClassOrInterfaceDeclaration owner,
      ClassOrInterfaceDeclaration function,
      String functionName,
      String helper) {
    var methods =
        function.getMethods().stream().filter(m -> !m.isStatic() && m.getBody().isEmpty()).toList();
    if (methods.size() != 1)
      throw new IllegalArgumentException("Expected a single callback method: " + functionName);
    var method = methods.get(0);
    List<String> params = new ArrayList<>(), names = new ArrayList<>(), values = new ArrayList<>();
    for (int i = 0; i < method.getParameters().size(); i++) {
      String type = resolve(method.getParameter(i).getTypeAsString(), function);
      String name = "a" + i;
      names.add(name);
      params.add((erasedValue(type) ? "org.teavm.jso.JSObject" : type) + " " + name);
      values.add(
          switch (type) {
            case "Integer", "java.lang.Integer" ->
                name
                    + " == null ? null : java.lang.Integer.valueOf(jsinterop.base.Js.asInt("
                    + name
                    + "))";
            case "Double", "java.lang.Double" ->
                name
                    + " == null ? null : java.lang.Double.valueOf(jsinterop.base.Js.asDouble("
                    + name
                    + "))";
            case "Boolean", "java.lang.Boolean" ->
                name
                    + " == null ? null : java.lang.Boolean.valueOf(jsinterop.base.Js.asBoolean("
                    + name
                    + "))";
            case "Object", "java.lang.Object" -> "jsinterop.base.Js.cast(" + name + ")";
            default -> name;
          });
    }
    String result = resolve(method.getTypeAsString(), function);
    String raw = helper + "Raw";
    String invoke = "callback." + method.getNameAsString() + "(" + String.join(",", values) + ")";
    if (erasedValue(result)) invoke = "jsinterop.base.Js.asAny(" + invoke + ")";
    owner.addMember(
        StaticJavaParser.parseBodyDeclaration(
            "@org.teavm.jso.JSFunctor public interface "
                + raw
                + " extends org.teavm.jso.JSObject { "
                + (erasedValue(result) ? "org.teavm.jso.JSObject" : result)
                + " call("
                + String.join(",", params)
                + "); }"));
    owner.addMember(
        StaticJavaParser.parseBodyDeclaration(
            "@org.teavm.jso.JSBody(params=\"callback\",script=\"return callback;\") private static native org.teavm.jso.JSObject "
                + helper
                + "Native("
                + raw
                + " callback);"));
    owner.addMember(
        StaticJavaParser.parseBodyDeclaration(
            "private static org.teavm.jso.JSObject "
                + helper
                + "("
                + functionName
                + " callback) {"
                + "if(callback == null) return null; return io.instanto.compat.NativeCallbacks.remember(callback,\"erased:"
                + functionName
                + "\","
                + helper
                + "Native(("
                + String.join(",", names)
                + ") -> "
                + invoke
                + "));}"));
  }

  private static boolean erasedValue(String type) {
    return Set.of(
            "Object",
            "java.lang.Object",
            "Integer",
            "java.lang.Integer",
            "Double",
            "java.lang.Double",
            "Boolean",
            "java.lang.Boolean")
        .contains(type);
  }

  private Type receiverType(
      Expression scope, MethodCallExpr call, ClassOrInterfaceDeclaration owner) {
    String name;
    if (scope.isNameExpr()) name = scope.asNameExpr().getNameAsString();
    else if (scope.isFieldAccessExpr() && scope.asFieldAccessExpr().getScope().isThisExpr())
      name = scope.asFieldAccessExpr().getNameAsString();
    else return null;
    // Fields are the common container case. Locals/parameters with the same name take precedence.
    if (scope.isNameExpr()) {
      // Lambda/catch parameters can hide a field without appearing in the enclosing method's
      // parameter list. Inferred lambda parameter types require symbol resolution; leave them
      // alone.
      for (Node node = call; node != owner; node = node.getParentNode().orElse(owner)) {
        if (node instanceof LambdaExpr lambda)
          for (var p : lambda.getParameters())
            if (p.getNameAsString().equals(name)) return p.getType();
        if (node instanceof CatchClause clause
            && clause.getParameter().getNameAsString().equals(name))
          return clause.getParameter().getType();
      }
      var callable = call.findAncestor(CallableDeclaration.class);
      if (callable.isPresent()) {
        for (var p : ((CallableDeclaration<?>) callable.get()).getParameters())
          if (p.getNameAsString().equals(name)) return p.getType();
        var locals =
            callable.get().findAll(VariableDeclarator.class).stream()
                .filter(
                    v ->
                        v.getNameAsString().equals(name)
                            && inScope(v, call)
                            && v.getRange().isPresent()
                            && call.getRange().isPresent()
                            && v.getRange().get().begin.isBefore(call.getRange().get().begin))
                .toList();
        if (locals.size() == 1) return locals.get(0).getType();
        if (locals.size() > 1) return null;
      }
    }
    for (var field : owner.getFields())
      for (var v : field.getVariables()) if (v.getNameAsString().equals(name)) return v.getType();
    return null;
  }

  private static boolean inScope(VariableDeclarator variable, MethodCallExpr call) {
    Node declaration = variable.getParentNode().orElse(null);
    if (!(declaration instanceof VariableDeclarationExpr)) return false;
    Node scope = declaration.getParentNode().orElse(null);
    if (scope instanceof ExpressionStmt) scope = scope.getParentNode().orElse(null);
    if (!(scope instanceof BlockStmt
        || scope instanceof ForStmt
        || scope instanceof ForEachStmt
        || scope instanceof TryStmt)) return false;
    for (Node node = call; node != null; node = node.getParentNode().orElse(null))
      if (node == scope) return true;
    return false;
  }

  private String resolve(String name, ClassOrInterfaceDeclaration owner) {
    if (name.endsWith("[]")) return resolve(name.substring(0, name.length() - 2), owner) + "[]";
    if (types.containsKey(name)) return name;
    var cu = owner.findCompilationUnit().orElseThrow();
    String scope = owner.getFullyQualifiedName().orElse("");
    while (!scope.isEmpty()) {
      if (types.containsKey(scope + "." + name)) return scope + "." + name;
      int dot = scope.lastIndexOf('.');
      scope = dot < 0 ? "" : scope.substring(0, dot);
    }
    for (var imp : cu.getImports()) {
      String imported = imp.getNameAsString();
      if (imp.isAsterisk()) {
        if (types.containsKey(imported + "." + name)) return imported + "." + name;
      } else {
        String simple = imp.getName().getIdentifier();
        if (name.equals(simple)) return imported;
        if (name.startsWith(simple + ".")) return imported + name.substring(simple.length());
      }
    }
    return name;
  }
}
