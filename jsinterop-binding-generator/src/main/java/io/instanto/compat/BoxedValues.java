package io.instanto.compat;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import java.util.Map;

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
      String kind = kind(variable.getTypeAsString(), imported);
      if (kind == null) continue;
      String primitive = kind.equals("Boolean") ? "boolean" : "double";
      throw new IllegalArgumentException(
          "Native field "
              + owner.getFullyQualifiedName().orElse(owner.getNameAsString())
              + "."
              + variable.getNameAsString()
              + " is declared "
              + kind
              + ". TeaVM would hand JavaScript a Java object instead of a "
              + (kind.equals("Boolean") ? "boolean" : "number")
              + ". Declare it "
              + primitive
              + ", or replace it with @JsProperty accessor methods, which are converted.");
    }
  }

  static boolean isConverted(String type, Map<String, String> imported) {
    return kind(type, imported) != null;
  }
}
