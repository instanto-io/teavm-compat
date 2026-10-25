package io.instanto.widgets.processor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.annotation.processing.ProcessingEnvironment;
import javax.imageio.ImageIO;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.*;

/** Generates cached ClientBundle resources and their ServiceLoader provider. */
final class ResourceBundleGenerator {
  private final ProcessingEnvironment env;

  ResourceBundleGenerator(ProcessingEnvironment env) {
    this.env = env;
  }

  void process(Element element) {
    if (!(element instanceof TypeElement type)) return;
    for (Element nested : type.getEnclosedElements())
      if (nested instanceof TypeElement) process(nested);
    if (type.getKind() != ElementKind.INTERFACE
        || !is(type, "com.google.gwt.resources.client.ClientBundle")
        || type.getQualifiedName().contentEquals("com.google.gwt.resources.client.ClientBundle"))
      return;
    try {
      generate(type);
    } catch (IOException | IllegalArgumentException failure) {
      env.getMessager()
          .printMessage(
              Diagnostic.Kind.ERROR,
              "Cannot generate bundle " + type.getQualifiedName() + ": " + failure.getMessage(),
              type);
    }
  }

  private boolean is(TypeElement type, String base) {
    TypeElement parent = env.getElementUtils().getTypeElement(base);
    return parent != null && env.getTypeUtils().isSubtype(type.asType(), parent.asType());
  }

  private void generate(TypeElement type) throws IOException {
    String pkg = env.getElementUtils().getPackageOf(type).getQualifiedName().toString();
    String binary = env.getElementUtils().getBinaryName(type).toString();
    String simple =
        binary.substring(pkg.isEmpty() ? 0 : pkg.length() + 1).replace('$', '_') + "_Resources";
    String qualified = pkg.isEmpty() ? simple : pkg + "." + simple;
    try (var in =
        env.getFiler()
            .getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + binary)
            .openInputStream()) {
      if (!new String(in.readAllBytes(), StandardCharsets.UTF_8).trim().equals(qualified)) return;
    } catch (IOException absent) {
      /* No explicit provider. */
    }
    StringBuilder source = new StringBuilder();
    if (!pkg.isEmpty()) source.append("package ").append(pkg).append(";\n");
    source
        .append("public final class ")
        .append(simple)
        .append(" implements ")
        .append(type.getQualifiedName())
        .append(" {\n");
    for (ExecutableElement method :
        ElementFilter.methodsIn(env.getElementUtils().getAllMembers(type))) {
      if (!method.getModifiers().contains(Modifier.ABSTRACT)) continue;
      appendResource(source, method, binary);
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

  private void appendResource(StringBuilder source, ExecutableElement method, String binary)
      throws IOException {
    if (!method.getParameters().isEmpty())
      throw new IllegalArgumentException("Resource methods must have no parameters: " + method);
    TypeElement resource = (TypeElement) env.getTypeUtils().asElement(method.getReturnType());
    if (resource == null) throw new IllegalArgumentException("Unsupported resource: " + method);
    String name = method.getSimpleName().toString();
    String result = method.getReturnType().toString();
    String resourcePackage =
        env.getElementUtils().getPackageOf(method).getQualifiedName().toString();
    List<String> paths = sources(method);
    String body = resourceBody(method, resource, result, name, binary, resourcePackage, paths);
    source
        .append("private static final ")
        .append(result)
        .append(" resource_")
        .append(name)
        .append("=new ")
        .append(result)
        .append("(){public String getName(){return ")
        .append(SourceSupport.quote(name))
        .append(";}")
        .append(body)
        .append("};\npublic ")
        .append(result)
        .append(' ')
        .append(name)
        .append("(){return resource_")
        .append(name)
        .append(";}\n");
  }

  private String resourceBody(
      ExecutableElement method,
      TypeElement resource,
      String result,
      String name,
      String binary,
      String resourcePackage,
      List<String> paths)
      throws IOException {
    if (is(resource, "com.google.gwt.resources.client.CssResource")) {
      StringBuilder css = new StringBuilder();
      for (String path : paths) css.append(readCss(env, resourcePackage, path)).append('\n');
      if (css.toString().contains("@import"))
        throw new IllegalArgumentException(
            "CSS @import must be resolved before generation: " + method);
      var styles = CssSupport.scope(css.toString(), SourceSupport.prefix(binary + "." + name));
      return cssBody(resource, name, styles);
    } else if (result.equals("com.google.gwt.resources.client.TextResource")) {
      StringBuilder text = new StringBuilder();
      for (String path : paths)
        text.append(
            new String(SourceSupport.read(env, resourcePackage, path), StandardCharsets.UTF_8));
      return "private final String text="
          + SourceSupport.stringExpression(text.toString())
          + "; public String getText(){return text;}";
    } else if (result.equals("com.google.gwt.resources.client.ImageResource")
        || result.equals("com.google.gwt.resources.client.DataResource")) {
      return binaryBody(method, result, resourcePackage, paths);
    } else throw new IllegalArgumentException("Unsupported resource type: " + result);
  }

  private String binaryBody(
      ExecutableElement method, String result, String resourcePackage, List<String> paths)
      throws IOException {
    String body;
    if (paths.size() != 1)
      throw new IllegalArgumentException("Binary resource requires one @Source: " + method);
    byte[] bytes = SourceSupport.read(env, resourcePackage, paths.get(0));
    String mime = java.net.URLConnection.guessContentTypeFromName(paths.get(0));
    if (mime == null) mime = "application/octet-stream";
    String uri = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    body =
        "private final String uri="
            + SourceSupport.stringExpression(uri)
            + "; private final com.google.gwt.safehtml.shared.SafeUri safeUri=()->uri; public"
            + " com.google.gwt.safehtml.shared.SafeUri getSafeUri(){return safeUri;}";
    if (result.endsWith("ImageResource")) {
      body += imageBody(bytes, paths.get(0));
    } else body += "public String getUrl(){return uri;}";
    return body;
  }

  private String imageBody(byte[] bytes, String path) throws IOException {
    var image = ImageIO.read(new ByteArrayInputStream(bytes));
    if (image == null)
      throw new IllegalArgumentException(
          "Image dimensions unavailable for " + path + "; use PNG, JPEG, GIF or BMP");
    boolean animated = false;
    try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      var readers = ImageIO.getImageReaders(input);
      if (readers.hasNext()) {
        var reader = readers.next();
        try {
          reader.setInput(input);
          animated = reader.getNumImages(true) > 1;
        } finally {
          reader.dispose();
        }
      }
    }
    return "public String getURL(){return uri;} public int getWidth(){return "
        + image.getWidth()
        + ";} public int getHeight(){return "
        + image.getHeight()
        + ";} public int getLeft(){return 0;} public int getTop(){return 0;} public"
        + " boolean isAnimated(){return "
        + animated
        + ";}";
  }

  /** Embeds local CSS assets relative to the stylesheet, leaving absolute URLs intact. */
  static String readCss(ProcessingEnvironment env, String pkg, String path) throws IOException {
    String css = new String(SourceSupport.read(env, pkg, path), StandardCharsets.UTF_8);
    if (css.contains("@import"))
      throw new IOException("CSS @import must be resolved before generation: " + path);
    StringBuilder result = new StringBuilder();
    int copied = 0;
    for (var url : CssSupport.urls(css)) {
      if (!absoluteUrl(url.value())) {
        result.append(css, copied, url.start());
        result.append(embedCssUrl(env, pkg, path, url.value()));
        copied = url.end();
      }
    }
    return result.append(css, copied, css.length()).toString();
  }

  private static boolean absoluteUrl(String url) {
    return url.startsWith("/") || url.startsWith("#") || url.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*");
  }

  private static String embedCssUrl(ProcessingEnvironment env, String pkg, String path, String url)
      throws IOException {
    String suffix = "";
    int hash = url.indexOf('#');
    if (hash >= 0) {
      suffix = url.substring(hash);
      url = url.substring(0, hash);
    }
    int query = url.indexOf('?');
    if (query >= 0) url = url.substring(0, query);
    String parent = path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) : "";
    String resolved = java.nio.file.Path.of(parent + url).normalize().toString().replace('\\', '/');
    byte[] bytes = SourceSupport.read(env, pkg, resolved);
    String mime = java.net.URLConnection.guessContentTypeFromName(url);
    if (mime == null) mime = "application/octet-stream";
    return "url(\"data:"
        + mime
        + ";base64,"
        + Base64.getEncoder().encodeToString(bytes)
        + suffix
        + "\")";
  }

  String cssBody(TypeElement type, String name, CssSupport.Styles styles) {
    StringBuilder body =
        new StringBuilder("private boolean injected; private final String text=")
            .append(SourceSupport.stringExpression(styles.css()))
            .append(
                "; public String getText(){return text;} public boolean"
                    + " ensureInjected(){if(injected)return"
                    + " false;com.google.gwt.dom.client.StyleInjector.inject(text,true);injected=true;return"
                    + " true;}");
    for (ExecutableElement method :
        ElementFilter.methodsIn(env.getElementUtils().getAllMembers(type))) {
      if (!method.getModifiers().contains(Modifier.ABSTRACT)
          || Set.of("getText", "getName", "ensureInjected")
              .contains(method.getSimpleName().toString())) continue;
      if (!method.getParameters().isEmpty()
          || !method.getReturnType().toString().equals("java.lang.String"))
        throw new IllegalArgumentException("CSS class accessors must return String: " + method);
      String cssName =
          SourceSupport.annotation(
              method, "com.google.gwt.resources.client.CssResource.ClassName", "value");
      if (cssName == null) cssName = method.getSimpleName().toString();
      String mapped = styles.classes().get(cssName);
      if (mapped == null)
        throw new IllegalArgumentException("CSS class ." + cssName + " not found for " + name);
      body.append("public String ")
          .append(method.getSimpleName())
          .append("(){return ")
          .append(SourceSupport.quote(mapped))
          .append(";}");
    }
    return body.toString();
  }

  private List<String> sources(ExecutableElement method) {
    for (var annotation : method.getAnnotationMirrors()) {
      if (!annotation
          .getAnnotationType()
          .toString()
          .equals("com.google.gwt.resources.client.ClientBundle.Source")) continue;
      for (var value : annotation.getElementValues().values()) {
        List<String> paths = new ArrayList<>();
        for (Object item : (List<?>) value.getValue())
          paths.add(((AnnotationValue) item).getValue().toString());
        if (!paths.isEmpty()) return paths;
      }
    }
    throw new IllegalArgumentException(method + " requires @Source");
  }
}
