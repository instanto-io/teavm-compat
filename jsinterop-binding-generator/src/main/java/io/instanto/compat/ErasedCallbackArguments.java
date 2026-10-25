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

  private record CallSite(
      Expression scope,
      ClassOrInterfaceDeclaration owner,
      ClassOrInterfaceType receiver,
      ClassOrInterfaceDeclaration nativeType,
      MethodDeclaration method,
      NodeList<Type> arguments) {}

  String transform(String source) {
    var cu = StaticJavaParser.parse(source);
    boolean changed = false;
    int sequence = 0;
    for (var call : new ArrayList<>(cu.findAll(MethodCallExpr.class))) {
      var site = callSite(call);
      if (site != null) {
        int next = adaptArguments(call, site, sequence);
        if (next != sequence) {
          var raw = site.receiver().clone();
          raw.removeTypeArguments();
          call.setScope(new EnclosedExpr(new CastExpr(raw, site.scope().clone())));
          sequence = next;
          changed = true;
        }
      }
    }
    return changed ? cu.toString() : source;
  }

  private CallSite callSite(MethodCallExpr call) {
    var scope = call.getScope().orElse(null);
    var owner = call.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
    if (scope == null || owner == null) return null;
    Type receiver = receiverType(scope, call, owner);
    if (!(receiver instanceof ClassOrInterfaceType declared)) return null;
    var arguments = declared.getTypeArguments().orElse(new NodeList<>());
    var nativeType = types.get(resolve(declared.getNameWithScope(), owner));
    if (arguments.isEmpty()
        || !isNativeType(nativeType)
        || arguments.size() != nativeType.getTypeParameters().size()) return null;
    var methods =
        nativeType.getMethodsByName(call.getNameAsString()).stream()
            .filter(
                m ->
                    m.isNative()
                        && m.getType().isVoidType()
                        && m.getParameters().size() == call.getArguments().size())
            .toList();
    // Do not guess overload resolution. A unique native void setter is unambiguous.
    return methods.size() == 1
        ? new CallSite(scope, owner, declared, nativeType, methods.get(0), arguments)
        : null;
  }

  private static boolean isNativeType(ClassOrInterfaceDeclaration type) {
    if (type == null) return false;
    var annotation = type.getAnnotationByName("JsType").orElse(null);
    return annotation instanceof NormalAnnotationExpr normal
        && normal.getPairs().stream()
            .anyMatch(
                pair ->
                    pair.getNameAsString().equals("isNative")
                        && pair.getValue().isBooleanLiteralExpr()
                        && pair.getValue().asBooleanLiteralExpr().getValue());
  }

  private int adaptArguments(MethodCallExpr call, CallSite site, int sequence) {
    for (int i = 0; i < site.method().getParameters().size(); i++) {
      String parameter = site.method().getParameter(i).getTypeAsString();
      int variable = typeParameterIndex(site.nativeType(), parameter);
      if (variable >= 0) {
        String functionName = resolve(site.arguments().get(variable).toString(), site.owner());
        var function = types.get(functionName);
        if (function != null && function.getAnnotationByName("JsFunction").isPresent()) {
          if (!function.getTypeParameters().isEmpty())
            throw new IllegalArgumentException(
                "Parameterized erased callback needs an explicit bridge: " + functionName);
          String helper = "$erasedCallback" + sequence++;
          addBridge(site.owner(), function, functionName, helper);
          call.setArgument(i, new MethodCallExpr(helper).addArgument(call.getArgument(i).clone()));
        }
      }
    }
    return sequence;
  }

  private static int typeParameterIndex(ClassOrInterfaceDeclaration type, String name) {
    for (int i = 0; i < type.getTypeParameters().size(); i++) {
      if (type.getTypeParameter(i).getNameAsString().equals(name)) return i;
    }
    return -1;
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
    List<String> params = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> values = new ArrayList<>();
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
    if (scope.isNameExpr()) {
      Type shadow = localType(call, owner, name);
      if (shadow != null) return shadow;
    }
    return owner.getFields().stream()
        .flatMap(f -> f.getVariables().stream())
        .filter(v -> v.getNameAsString().equals(name))
        .map(VariableDeclarator::getType)
        .findFirst()
        .orElse(null);
  }

  private Type localType(MethodCallExpr call, ClassOrInterfaceDeclaration owner, String name) {
    Type shadow = enclosingParameterType(call, owner, name);
    if (shadow != null) return shadow;
    var callable = call.findAncestor(CallableDeclaration.class).orElse(null);
    if (callable == null) return null;
    Type parameter = parameterType(((CallableDeclaration<?>) callable).getParameters(), name);
    if (parameter != null) return parameter;
    var locals =
        callable.findAll(VariableDeclarator.class).stream()
            .filter(v -> v.getNameAsString().equals(name) && inScope(v, call) && precedes(v, call))
            .toList();
    // An ambiguous local still shadows a field; keep it unresolved rather than falling back.
    if (locals.size() > 1) return new UnknownType();
    return locals.size() == 1 ? locals.get(0).getType() : null;
  }

  private static boolean precedes(Node declaration, Node use) {
    var declarationRange = declaration.getRange().orElse(null);
    var useRange = use.getRange().orElse(null);
    return declarationRange != null
        && useRange != null
        && declarationRange.begin.isBefore(useRange.begin);
  }

  private static Type parameterType(NodeList<Parameter> parameters, String name) {
    return parameters.stream()
        .filter(p -> p.getNameAsString().equals(name))
        .map(Parameter::getType)
        .findFirst()
        .orElse(null);
  }

  private static Type enclosingParameterType(
      MethodCallExpr call, ClassOrInterfaceDeclaration owner, String name) {
    // Lambda and catch parameters may hide fields; inferred types deliberately remain unknown.
    for (Node node = call; node != owner; node = node.getParentNode().orElse(owner)) {
      if (node instanceof LambdaExpr lambda) {
        Type type = parameterType(lambda.getParameters(), name);
        if (type != null) return type;
      }
      if (node instanceof CatchClause clause
          && clause.getParameter().getNameAsString().equals(name))
        return clause.getParameter().getType();
    }
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
      String resolved = importedName(imp, name);
      if (resolved != null) return resolved;
    }
    return name;
  }

  private String importedName(ImportDeclaration imp, String name) {
    String imported = imp.getNameAsString();
    if (imp.isAsterisk())
      return types.containsKey(imported + "." + name) ? imported + "." + name : null;
    String simple = imp.getName().getIdentifier();
    if (name.equals(simple)) return imported;
    return name.startsWith(simple + ".") ? imported + name.substring(simple.length()) : null;
  }
}
