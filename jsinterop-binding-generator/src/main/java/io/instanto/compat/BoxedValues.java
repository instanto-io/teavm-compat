package io.instanto.compat;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Boxed numbers and booleans in native JsInterop declarations.
 *
 * <p>GWT passes a {@code Double}, {@code Number} or {@code Boolean} to JavaScript as a plain number
 * or boolean. TeaVM passes the Java object itself, so a native member declared with one of these
 * types needs a conversion at the native boundary.
 */
final class BoxedValues {
  private static final Map<String, String> RESULTS =
      Map.of(
          "Double", "java.lang.Double.valueOf(jsinterop.base.Js.asDouble(%s))",
          "Number", "java.lang.Double.valueOf(jsinterop.base.Js.asDouble(%s))",
          "Float", "java.lang.Float.valueOf(jsinterop.base.Js.asFloat(%s))",
          "Integer", "java.lang.Integer.valueOf(jsinterop.base.Js.asInt(%s))",
          "Short", "java.lang.Short.valueOf(jsinterop.base.Js.asShort(%s))",
          "Byte", "java.lang.Byte.valueOf(jsinterop.base.Js.asByte(%s))",
          "Boolean", "java.lang.Boolean.valueOf(jsinterop.base.Js.asBoolean(%s))");

  private static final Map<String, String> PRIMITIVES =
      Map.of(
          "Double", "double",
          "Number", "double",
          "Float", "float",
          "Integer", "int",
          "Short", "short",
          "Byte", "byte",
          "Boolean", "boolean");

  /** The JavaScript-side type of a converted value at the bridge. */
  static final String NATIVE_TYPE = "jsinterop.base.Any";

  private BoxedValues() {}

  /**
   * The {@code java.lang} simple name when {@code type} is a converted box, else {@code null}.
   *
   * @param imported simple names this source imports or declares, which shadow {@code java.lang}
   */
  static String kind(String type, Map<String, String> imported) {
    String simple = type.startsWith("java.lang.") ? type.substring("java.lang.".length()) : type;
    if (!RESULTS.containsKey(simple)) return null;
    if (type.equals(simple)
        && imported.containsKey(simple)
        && !imported.get(simple).equals("java.lang." + simple)) return null;
    return simple;
  }

  /** How many array dimensions {@code type} has, counting a varargs parameter as one. */
  static int dimensions(String type) {
    int count = 0;
    while (type.endsWith("[]")) {
      type = type.substring(0, type.length() - 2).strip();
      count++;
    }
    return count;
  }

  /** The box kind of {@code type} or of its array elements, else {@code null}. */
  static String elementKind(String type, Map<String, String> imported) {
    return kind(type.replace("[]", "").strip(), imported);
  }

  /** The declared type of a parameter, with varargs written as an array. */
  static String typeOf(Parameter parameter) {
    return parameter.getTypeAsString() + (parameter.isVarArgs() ? "[]" : "");
  }

  /** Converts a bridge result to the declared box. Null and undefined both become null. */
  static String result(String kind, String value) {
    return value
        + " == null || \"undefined\".equals(jsinterop.base.Js.typeof("
        + value
        + ")) ? null : "
        + RESULTS.get(kind).formatted(value);
  }

  /** Converts a declared box to the JavaScript primitive it represents. Null stays null. */
  static String argument(String value) {
    return "jsinterop.base.Js.asAny(" + value + ")";
  }

  /** Converts an array of boxes element by element into a JavaScript array of primitives. */
  static String arrayArgument(String value) {
    return value
        + " == null ? null : java.util.Arrays.stream("
        + value
        + ").map(jsinterop.base.Js::asAny).toArray("
        + NATIVE_TYPE
        + "[]::new)";
  }

  /** Converts a bridge array result back to an array of the declared box. */
  static String arrayResult(String kind, String value) {
    return value
        + " == null ? null : java.util.Arrays.stream("
        + value
        + ").map($v -> "
        + result(kind, "$v")
        + ").toArray(java.lang."
        + kind
        + "[]::new)";
  }

  /** Simple names a source imports or declares, so a library's own {@code Number} is left alone. */
  static Map<String, String> shadowing(CompilationUnit cu) {
    var names = new java.util.HashMap<String, String>();
    cu.getImports().stream()
        .filter(i -> !i.isAsterisk() && !i.isStatic())
        .forEach(i -> names.put(i.getName().getIdentifier(), i.getNameAsString()));
    cu.findAll(ClassOrInterfaceDeclaration.class)
        .forEach(t -> names.put(t.getNameAsString(), t.getFullyQualifiedName().orElse("")));
    return names;
  }

  /**
   * A native field is read and written where the caller compiles, so there is no boundary to
   * convert at. Changing its Java type would change what callers may assign. Reject it by name.
   */
  static void rejectField(
      ClassOrInterfaceDeclaration owner, FieldDeclaration field, Map<String, String> imported) {
    for (var variable : field.getVariables()) {
      String type = variable.getTypeAsString();
      String kind = elementKind(type, imported);
      if (kind == null) continue;
      throw new IllegalArgumentException(
          "Native field "
              + name(owner)
              + "."
              + variable.getNameAsString()
              + " is declared "
              + type
              + ". TeaVM would hand JavaScript "
              + handed(kind, type)
              + ". Declare it "
              + primitive(kind, type)
              + ", or replace it with @JsProperty accessor methods, which are converted.");
    }
  }

  /**
   * TeaVM turns {@code new NativeType(...)} into a JavaScript {@code new} where the caller
   * compiles, passing each argument by its declared type, and never runs the constructor body. So
   * there is nowhere to convert a boxed argument. Reject it by name.
   */
  static void rejectConstructor(
      ClassOrInterfaceDeclaration owner,
      ConstructorDeclaration constructor,
      Map<String, String> imported) {
    for (var parameter : constructor.getParameters()) {
      String type = typeOf(parameter);
      String kind = elementKind(type, imported);
      if (kind == null) continue;
      throw new IllegalArgumentException(
          "Native constructor "
              + name(owner)
              + "("
              + constructor.getParameters().stream()
                  .map(BoxedValues::typeOf)
                  .collect(Collectors.joining(", "))
              + ") declares parameter "
              + parameter.getNameAsString()
              + " as "
              + type
              + ". TeaVM passes constructor arguments by their declared type, so JavaScript would"
              + " receive "
              + handed(kind, type)
              + ". Declare it "
              + primitive(kind, type)
              + (dimensions(type) == 0
                  ? ", and add an overload without it where null was meant."
                  : "."));
    }
  }

  /**
   * Methods convert boxes and one-dimensional arrays of boxes. Deeper arrays are rejected by name
   * rather than passed as nested arrays of Java objects.
   */
  static void rejectNested(
      ClassOrInterfaceDeclaration owner,
      String method,
      String what,
      String type,
      Map<String, String> imported) {
    String kind = elementKind(type, imported);
    if (kind == null || dimensions(type) < 2) return;
    throw new IllegalArgumentException(
        "Native method "
            + name(owner)
            + "."
            + method
            + " declares "
            + what
            + " as "
            + type
            + ". Only boxes and one-dimensional arrays of boxes are converted. Declare it "
            + primitive(kind, type)
            + ".");
  }

  private static String name(ClassOrInterfaceDeclaration owner) {
    return owner.getFullyQualifiedName().orElse(owner.getNameAsString());
  }

  private static String handed(String kind, String type) {
    String value = kind.equals("Boolean") ? "boolean" : "number";
    return dimensions(type) == 0
        ? "a Java object instead of a " + value
        : "an array of Java objects instead of " + value + "s";
  }

  private static String primitive(String kind, String type) {
    return PRIMITIVES.get(kind) + "[]".repeat(dimensions(type));
  }

  static boolean isConverted(String type, Map<String, String> imported) {
    return kind(type, imported) != null;
  }
}
