package io.instanto.compat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import java.util.regex.Pattern;

/** Converts JSNI bodies without Java member references; unsupported Java callbacks fail closed. */
public final class LegacyNativeBodies {
  private LegacyNativeBodies() {}

  public static String transform(String source) {
    if (!source.contains("/*-{")) return source;
    var cu = StaticJavaParser.parse(source);
    cu.addImport("org.teavm.jso.JSBody");
    Pattern body = Pattern.compile("/\\*-\\{(.*?)\\}-\\*/", Pattern.DOTALL);
    int translated = 0;
    for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
      var match = body.matcher(method.getTokenRange().orElseThrow().toString());
      if (!match.find()) continue;
      if (!method.isNative())
        throw new IllegalArgumentException("JSNI on non-native method " + method.getName());
      String script = match.group(1).trim();
      if (script.contains("@"))
        throw new IllegalArgumentException(
            "JSNI Java member references need an explicit bridge: " + method.getName());
      script = script.replace("$wnd", "window").replace("$doc", "document");
      NodeList<Expression> params = new NodeList<>();
      method.getParameters().forEach(p -> params.add(new StringLiteralExpr(p.getNameAsString())));
      method.addAnnotation(
          new NormalAnnotationExpr(
              new Name("JSBody"),
              NodeList.nodeList(
                  new MemberValuePair("params", new ArrayInitializerExpr(params)),
                  new MemberValuePair("script", new StringLiteralExpr().setString(script)))));
      translated++;
    }
    if (translated != source.split("/\\*-\\{", -1).length - 1)
      throw new IllegalArgumentException("Untranslated JSNI body");
    return cu.toString();
  }
}
