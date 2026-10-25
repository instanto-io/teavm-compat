package io.instanto.widgets.processor;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.*;

/** Generates text/quoted-attribute templates with context-aware escaping. */
final class SafeHtmlTemplateGenerator {
  private final ProcessingEnvironment env;

  SafeHtmlTemplateGenerator(ProcessingEnvironment env) {
    this.env = env;
  }

  void process(Element element) {
    if (!(element instanceof TypeElement type)) return;
    for (Element nested : type.getEnclosedElements())
      if (nested instanceof TypeElement) process(nested);
    TypeElement base =
        env.getElementUtils().getTypeElement("com.google.gwt.safehtml.client.SafeHtmlTemplates");
    if (base == null
        || type.equals(base)
        || type.getKind() != ElementKind.INTERFACE
        || !env.getTypeUtils().isSubtype(type.asType(), base.asType())) return;
    if (hasExplicitProvider(type)) return;
    try {
      generate(type);
    } catch (IOException | IllegalArgumentException failure) {
      env.getMessager()
          .printMessage(
              Diagnostic.Kind.ERROR,
              "Cannot generate SafeHtmlTemplates: " + failure.getMessage(),
              type);
    }
  }

  /** Existing hand-written bindings take precedence over generator fallbacks. */
  private boolean hasExplicitProvider(TypeElement type) {
    String binary = env.getElementUtils().getBinaryName(type).toString();
    String generated = binary.replace('$', '_') + "_Templates";
    try {
      String services =
          new String(
              SourceSupport.read(env, "", "META-INF/services/" + binary),
              java.nio.charset.StandardCharsets.UTF_8);
      for (String line : services.split("\\R")) {
        String providerName = line.split("#", 2)[0].trim();
        if (providerName.isEmpty() || providerName.equals(generated)) continue;
        TypeElement provider = env.getElementUtils().getTypeElement(providerName);
        if (provider != null
            && provider.getKind() == ElementKind.CLASS
            && !provider.getModifiers().contains(Modifier.ABSTRACT)
            && env.getTypeUtils().isSubtype(provider.asType(), type.asType())) return true;
      }
    } catch (IOException absent) {
      // No explicit binding: generate and validate the template normally.
    }
    return false;
  }

  private void generate(TypeElement type) throws IOException {
    String pkg = env.getElementUtils().getPackageOf(type).getQualifiedName().toString();
    String binary = env.getElementUtils().getBinaryName(type).toString();
    String simple =
        binary.substring(pkg.isEmpty() ? 0 : pkg.length() + 1).replace('$', '_') + "_Templates";
    String qualified = pkg.isEmpty() ? simple : pkg + "." + simple;
    StringBuilder source = new StringBuilder();
    if (!pkg.isEmpty()) source.append("package ").append(pkg).append(";\n");
    source
        .append("public final class ")
        .append(simple)
        .append(" implements ")
        .append(type.getQualifiedName())
        .append(" {\n");
    for (var method : ElementFilter.methodsIn(env.getElementUtils().getAllMembers(type))) {
      if (!method.getModifiers().contains(Modifier.ABSTRACT)) continue;
      if (!method.getReturnType().toString().equals("com.google.gwt.safehtml.shared.SafeHtml"))
        throw new IllegalArgumentException(method + " must return SafeHtml");
      String template =
          SourceSupport.annotation(
              method, "com.google.gwt.safehtml.client.SafeHtmlTemplates.Template", "value");
      if (template == null) throw new IllegalArgumentException(method + " requires @Template");
      if (Pattern.compile(
              "(?is)<\\s*(script|style|iframe|object|embed|meta|base|link|svg|math|animate|set|foreignobject)\\b")
          .matcher(template)
          .find())
        throw new IllegalArgumentException(
            "Active-content template elements are unsupported: " + method);
      source
          .append("public com.google.gwt.safehtml.shared.SafeHtml ")
          .append(method.getSimpleName())
          .append('(');
      StringJoiner parameters = new StringJoiner(",");
      for (int i = 0; i < method.getParameters().size(); i++)
        parameters.add(method.getParameters().get(i).asType() + " p" + i);
      source.append(parameters).append("){StringBuilder html=new StringBuilder();\n");
      Matcher placeholder = Pattern.compile("\\{(\\d+)\\}").matcher(template);
      int end = 0;
      while (placeholder.find()) {
        source
            .append("html.append(")
            .append(SourceSupport.quote(template.substring(end, placeholder.start())))
            .append(");\n");
        int index = Integer.parseInt(placeholder.group(1));
        if (index >= method.getParameters().size())
          throw new IllegalArgumentException("Missing parameter for " + placeholder.group());
        String value = "p" + index,
            typeName = method.getParameters().get(index).asType().toString();
        Context context = context(template, placeholder.start());
        boolean safeHtml = typeName.equals("com.google.gwt.safehtml.shared.SafeHtml");
        boolean safeUri = typeName.equals("com.google.gwt.safehtml.shared.SafeUri");
        if (safeHtml && context.attribute != null)
          throw new IllegalArgumentException("SafeHtml placeholder in an attribute: " + method);
        if (safeHtml) source.append("html.append(").append(value).append(".asString());\n");
        else {
          value = safeUri ? value + ".asString()" : "String.valueOf(" + value + ")";
          if (context.attribute != null
              && Set.of(
                      "href",
                      "src",
                      "action",
                      "formaction",
                      "poster",
                      "cite",
                      "background",
                      "data",
                      "xlink:href")
                  .contains(context.attribute)) {
            if (context.valueStart != placeholder.start()
                || placeholder.end() >= template.length()
                || template.charAt(placeholder.end()) != context.quote)
              throw new IllegalArgumentException(
                  "URI placeholders must occupy the entire attribute value: " + method);
            if (!safeUri)
              value = "com.google.gwt.safehtml.shared.UriUtils.sanitizeUri(" + value + ")";
          }
          source
              .append("html.append(com.google.gwt.safehtml.shared.SafeHtmlUtils.htmlEscape(")
              .append(value)
              .append("));\n");
        }
        end = placeholder.end();
      }
      source
          .append("html.append(")
          .append(SourceSupport.quote(template.substring(end)))
          .append(
              ");return"
                  + " com.google.gwt.safehtml.shared.SafeHtmlUtils.fromTrustedString(html.toString());}\n");
    }
    source.append("}\n");
    try (var writer = env.getFiler().createSourceFile(qualified, type).openWriter()) {
      writer.write(source.toString());
    }
    try (var writer =
        env.getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + binary, type)
            .openWriter()) {
      writer.write(qualified + "\n");
    }
  }

  private record Context(String attribute, char quote, int valueStart) {}

  private Context context(String template, int position) {
    boolean tag = false, comment = false;
    char quote = 0;
    int valueStart = 0, tagStart = 0;
    String attribute = null;
    for (int i = 0; i < position; i++) {
      if (comment) {
        if (template.startsWith("-->", i)) {
          comment = false;
          i += 2;
        }
        continue;
      }
      if (quote != 0) {
        if (template.charAt(i) == quote) {
          quote = 0;
          attribute = null;
        }
        continue;
      }
      if (template.startsWith("<!--", i)) {
        comment = true;
        i += 3;
        continue;
      }
      char c = template.charAt(i);
      if (c == '<') {
        tag = true;
        tagStart = i;
      } else if (c == '>') tag = false;
      else if (tag && (c == '\'' || c == '"')) {
        Matcher name =
            Pattern.compile("([\\w:-]+)\\s*=\\s*$").matcher(template.substring(tagStart, i));
        if (!name.find()) throw new IllegalArgumentException("Unrecognised attribute context");
        attribute = name.group(1).toLowerCase(Locale.ROOT);
        quote = c;
        valueStart = i + 1;
      }
    }
    if (comment || (tag && quote == 0))
      throw new IllegalArgumentException(
          "Placeholders must occur in text or quoted attribute values");
    if (attribute != null
        && (attribute.startsWith("on")
            || Set.of("style", "srcdoc", "srcset", "imagesrcset", "xmlns").contains(attribute)))
      throw new IllegalArgumentException("Unsafe placeholder context: " + attribute);
    return new Context(attribute, quote, valueStart);
  }
}
