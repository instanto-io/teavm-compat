/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.instanto.teavm.modules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Generates the TeaVM counterpart of a GWT module's resource declarations.
 *
 * <p>A {@code .gwt.xml} says which stylesheets a module needs, and its ClientBundle says which
 * scripts; GWT reads both and injects them. TeaVM has no module system to read them, so this reads
 * the same two declarations at build time and writes a class that loads the same files by URL --
 * and, unlike GWT, reports when they are usable.
 *
 * <p>The assets are copied out alongside, both to where the development server serves them and into
 * the jar, so a downstream application gets them by depending on it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GenerateModulesMojo extends AbstractMojo {

  /** Where the GWT modules are, normally the shared widget sources. */
  @Parameter(required = true)
  private java.io.File sourceRoot;

  /** Where the generated Java goes; add it as a source root. */
  @Parameter(required = true)
  private java.io.File outputRoot;

  /** Where the assets go for serving during development. */
  @Parameter(required = true)
  private java.io.File assetRoot;

  /** Where the assets go to be packaged into the jar. Optional. */
  @Parameter private java.io.File packagedRoot;

  /** The class every generated module delegates its loading to. */
  @Parameter(required = true)
  private String resourcesClass;

  /**
   * Which modules to generate for, by module name. All of them when empty.
   *
   * <p>A source tree usually holds more module descriptors than a backend has ported. Naming them
   * keeps the generated classes to the modules that are ready.
   */
  @Parameter private java.util.List<String> includeModules;

  /** Prefix for the element ids the generated classes use. */
  @Parameter(defaultValue = "gwt-resource-")
  private String idPrefix;

  /** Optional Java method references that recognise scripts already supplied by a host. */
  @Parameter private java.util.Map<String, String> scriptPresence;

  /** Text-only ClientBundles whose synchronous API is retained through generated providers. */
  @Parameter private java.util.List<String> inlineTextBundles;

  @Parameter(defaultValue = "${project.build.outputDirectory}")
  private java.io.File classOutput;

  private static final Pattern BUNDLE_SOURCE =
      Pattern.compile(
          "@(?:[\\w.]+\\.)?Source\\s*\\(\\s*(?:value\\s*=\\s*)?(\\{[^}]*\\}|\"[^\"]*\")\\s*\\)");
  private static final Pattern SOURCE_LITERAL = Pattern.compile("\"([^\"]+)\"");

  private record BundleAsset(Path file, String path, boolean inline) {}

  @Override
  public void execute() throws MojoExecutionException {
    if (!sourceRoot.isDirectory()) {
      throw new MojoExecutionException("sourceRoot is not a directory: " + sourceRoot);
    }
    int generated = 0;
    try (Stream<Path> paths = Files.walk(sourceRoot.toPath())) {
      final List<Path> modules = new ArrayList<>();
      paths.filter(p -> p.getFileName().toString().endsWith(".gwt.xml")).forEach(modules::add);
      modules.sort(Path::compareTo);
      for (final Path module : modules) {
        if (generate(module)) {
          generated++;
        }
      }
    } catch (final IOException problem) {
      throw new MojoExecutionException("could not read " + sourceRoot, problem);
    }
    getLog().info("generated " + generated + " TeaVM module" + (generated == 1 ? "" : "s"));
  }

  private boolean generate(final Path modulePath) throws MojoExecutionException {
    final Path dir = modulePath.getParent();
    final String fileName = modulePath.getFileName().toString();
    final String module = fileName.substring(0, fileName.length() - ".gwt.xml".length());
    if (includeModules != null && !includeModules.isEmpty() && !includeModules.contains(module)) {
      return false;
    }
    final Document document = parse(modulePath);

    final List<String> stylesheets = new ArrayList<>();
    final NodeList sheets = document.getElementsByTagName("stylesheet");
    for (int i = 0; i < sheets.getLength(); i++) {
      final String src = ((Element) sheets.item(i)).getAttribute("src");
      if (!src.isEmpty()) {
        stylesheets.add(src);
      }
    }
    final Set<String> scriptNames = new LinkedHashSet<>();
    final NodeList declaredScripts = document.getElementsByTagName("script");
    for (int i = 0; i < declaredScripts.getLength(); i++) {
      final String src = ((Element) declaredScripts.item(i)).getAttribute("src");
      if (!src.isEmpty()) {
        scriptNames.add(src);
      }
    }
    final List<BundleAsset> bundled = bundleAssets(dir);
    for (final BundleAsset asset : bundled) {
      if (asset.path().endsWith(".js") && !asset.inline()) {
        scriptNames.add(asset.path());
      }
    }
    final List<String> scripts = new ArrayList<>(scriptNames);
    if (stylesheets.isEmpty()
        && scripts.isEmpty()
        && bundled.isEmpty()
        && document.getElementsByTagName("public").getLength() == 0) {
      return false;
    }

    // A descriptor normally sits above the client package it names. Some sit inside
    // it, and appending another "client" to those produced a package that does not
    // exist and a class nothing could refer to.
    final String relative =
        sourceRoot.toPath().relativize(dir).toString().replace(java.io.File.separatorChar, '.');
    final String pkg = relative.endsWith(".client") ? relative : relative + ".client";
    final String klass = module + "Resources";
    final String prefix = idPrefix + module.toLowerCase(Locale.ROOT) + "-";

    write(
        outputRoot
            .toPath()
            .resolve(pkg.replace('.', java.io.File.separatorChar))
            .resolve(klass + ".java"),
        render(fileName, pkg, module, klass, prefix, stylesheets, scripts));

    copyAssets(document, dir);
    for (final BundleAsset asset : bundled) {
      for (final Path target : targets()) {
        copyInto(asset.file(), target.resolve(asset.path()).getParent());
      }
    }

    getLog()
        .info(
            "  "
                + fileName
                + " -> "
                + klass
                + " ("
                + stylesheets.size()
                + " stylesheet"
                + (stylesheets.size() == 1 ? "" : "s")
                + ", "
                + scripts.size()
                + " script"
                + (scripts.size() == 1 ? "" : "s")
                + ")");
    return true;
  }

  /**
   * The resources a module's ClientBundle declares on GWT.
   *
   * <p>They are declared in Java rather than in the module file, because on GWT they are compiled
   * into the output rather than served. Keep non-script resources too: images, data and styles
   * belong in the consuming application's asset tree.
   */
  private List<BundleAsset> bundleAssets(final Path dir) throws MojoExecutionException {
    final List<BundleAsset> found = new ArrayList<>();
    try (Stream<Path> paths = Files.walk(dir)) {
      final List<Path> bundles = new ArrayList<>();
      paths.filter(p -> p.getFileName().toString().endsWith(".java")).forEach(bundles::add);
      bundles.sort(Path::compareTo);
      for (final Path bundle : bundles) {
        final String source = Files.readString(bundle, StandardCharsets.UTF_8);
        if (!Pattern.compile("\\bextends\\s+[^{;]*\\bClientBundle\\b").matcher(source).find()) {
          continue;
        }
        final Matcher packageMatcher = Pattern.compile("package\\s+([\\w.]+)\\s*;").matcher(source);
        final String packageName = packageMatcher.find() ? packageMatcher.group(1) : "";
        final String simpleName = bundle.getFileName().toString().replace(".java", "");
        final boolean inline =
            inlineTextBundles != null && inlineTextBundles.contains(packageName + "." + simpleName);
        if (inline) {
          generateTextBundle(bundle, source, packageName, simpleName);
        }
        final Matcher matcher = BUNDLE_SOURCE.matcher(source);
        while (matcher.find()) {
          final Matcher literal = SOURCE_LITERAL.matcher(matcher.group(1));
          while (literal.find()) {
            final String path = literal.group(1);
            final Path file = bundle.getParent().resolve(path).normalize();
            if (!Files.isRegularFile(file)) {
              throw new MojoExecutionException(
                  "Missing ClientBundle resource " + path + " in " + bundle);
            }
            String deployed = path.startsWith("resource/") ? path.substring(9) : path;
            if (Path.of(deployed).isAbsolute() || deployed.contains("..")) {
              throw new MojoExecutionException(
                  "Unsupported resource path " + path + " in " + bundle);
            }
            found.add(new BundleAsset(file, deployed, inline));
          }
        }
      }
    } catch (final IOException problem) {
      throw new MojoExecutionException("could not scan " + dir, problem);
    }
    return found;
  }

  private void generateTextBundle(Path bundle, String source, String packageName, String simpleName)
      throws IOException, MojoExecutionException {
    final String implementation = simpleName + "_TeaVM";
    final StringBuilder java =
        new StringBuilder(
            "package "
                + packageName
                + ";\n"
                + "// Generated from ClientBundle declarations.\n"
                + "public final class "
                + implementation
                + " implements "
                + simpleName
                + " {\n");
    final Matcher methods =
        Pattern.compile("@Source\\(\"([^\"]+)\"\\)\\s*TextResource\\s+(\\w+)\\(\\)\\s*;")
            .matcher(source);
    int count = 0;
    while (methods.find()) {
      count++;
      final Path file = bundle.getParent().resolve(methods.group(1)).normalize();
      if (!file.startsWith(sourceRoot.toPath().normalize()) || !Files.isRegularFile(file)) {
        throw new MojoExecutionException("Invalid text resource " + file);
      }
      final String text = Files.readString(file, StandardCharsets.UTF_8);
      final String method = methods.group(2);
      java.append("public com.google.gwt.resources.client.TextResource ")
          .append(method)
          .append("() { return new com.google.gwt.resources.client.TextResource() {\n")
          .append("public String getName() { return \"")
          .append(method)
          .append("\"; }\n")
          .append("public String getText() { StringBuilder text = new StringBuilder();\n");
      // Separate append calls avoid the class-file limit on a single string constant.
      for (int start = 0; start < text.length(); start += 8000) {
        java.append("text.append(\"")
            .append(javaLiteral(text.substring(start, Math.min(start + 8000, text.length()))))
            .append("\");\n");
      }
      java.append("return text.toString(); }\n}; }\n");
    }
    long declarations =
        Pattern.compile("\\bTextResource\\s+\\w+\\(\\)\\s*;").matcher(source).results().count();
    if (count == 0 || count != declarations) {
      throw new MojoExecutionException(
          "Inline text bundles require literal @Source on every TextResource: " + bundle);
    }
    java.append("}\n");
    final Path target =
        outputRoot
            .toPath()
            .resolve(packageName.replace('.', '/'))
            .resolve(implementation + ".java");
    Files.createDirectories(target.getParent());
    Files.writeString(target, java, StandardCharsets.UTF_8);
    final Path service =
        classOutput.toPath().resolve("META-INF/services/" + packageName + "." + simpleName);
    Files.createDirectories(service.getParent());
    Files.writeString(service, packageName + "." + implementation + "\n", StandardCharsets.UTF_8);
  }

  private static String javaLiteral(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\f", "\\f");
  }

  private String render(
      final String moduleFile,
      final String pkg,
      final String module,
      final String klass,
      final String prefix,
      final List<String> stylesheets,
      final List<String> scripts) {
    final StringBuilder body = new StringBuilder();
    for (final String sheet : stylesheets) {
      body.append("            .stylesheet(").append(assetUrl(sheet, "css")).append(")\n");
    }
    for (final String script : scripts) {
      body.append("            .script(").append(assetUrl(script, "js"));
      if (scriptPresence != null && scriptPresence.containsKey(fileNameOf(script))) {
        body.append(", ").append(scriptPresence.get(fileNameOf(script)));
      }
      body.append(")\n");
    }
    // Trailing newline is trimmed so the chained call ends cleanly.
    if (body.length() > 0) {
      body.setLength(body.length() - 1);
    }

    return "// Generated from "
        + moduleFile
        + ". Do not edit.\n"
        + "package "
        + pkg
        + ";\n\n"
        + "import "
        + resourcesClass
        + ";\n"
        + "import io.instanto.teavm.module.ScriptModule;\n\n"
        + "/**\n"
        + " * Loads what the "
        + module
        + " module declares, on the TeaVM backend.\n"
        + " *\n"
        + " * <p>GWT injects these from "
        + moduleFile
        + " and the module's entry point.\n"
        + " * This is the TeaVM equivalent, fetching them by URL from wherever the module\n"
        + " * was deployed. An application does not call it; the widgets do, when they are\n"
        + " * constructed.</p>\n"
        + " *\n"
        + " * <p>Unlike the GWT side it also says when the module is <em>usable</em>. A\n"
        + " * script element reports when it has run and when it has failed, so a widget can\n"
        + " * wait for its library rather than poll for it, and a module that cannot load\n"
        + " * says so once instead of timing out into silence.</p>\n"
        + " */\n"
        + "public final class "
        + klass
        + " {\n\n"
        + "    private static final ScriptModule MODULE = ScriptModule.named(\""
        + module
        + "\")"
        + (body.length() == 0 ? "" : "\n" + body)
        + ";\n\n"
        + "    private "
        + klass
        + "() {\n"
        + "    }\n\n"
        + "    /** Injects this module's resources once; further calls do nothing. */\n"
        + "    public static void ensureInjected() {\n"
        + "        MODULE.ensureLoaded();\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Runs an action once this module's library is usable, now if it already is.\n"
        + "     *\n"
        + "     * <p>The presence test lets a page that loaded the library itself be\n"
        + "     * recognised without fetching a second copy, which is what keeps this\n"
        + "     * module system optional rather than compulsory.</p>\n"
        + "     */\n"
        + "    public static void whenReady(final ScriptModule.Presence presence,\n"
        + "            final Runnable action) {\n"
        + "        MODULE.presence(presence).whenReady(action);\n"
        + "    }\n\n"
        + "    /** Whether the library is usable now. */\n"
        + "    public static boolean isReady(final ScriptModule.Presence presence) {\n"
        + "        return MODULE.presence(presence).isReady();\n"
        + "    }\n"
        + "}\n";
  }

  private String assetUrl(String path, String kind) {
    String relative =
        path.startsWith(kind + "/") ? path.substring(kind.length() + 1) : "../" + path;
    return shortName(resourcesClass) + "." + kind + "Base() + \"" + relative + "\"";
  }

  /** Copies declared public assets with their paths and include/exclude rules preserved. */
  private void copyAssets(final Document document, final Path dir) throws MojoExecutionException {
    final NodeList publics = document.getElementsByTagName("public");
    for (int i = 0; i < publics.getLength(); i++) {
      final Element declaration = (Element) publics.item(i);
      final Path base = dir.resolve(declaration.getAttribute("path"));
      for (final Path target : targets()) {
        copyPublic(base, target, declaration);
      }
    }
  }

  private List<Path> targets() {
    final List<Path> targets = new ArrayList<>();
    targets.add(assetRoot.toPath());
    if (packagedRoot != null) {
      targets.add(packagedRoot.toPath());
    }
    return targets;
  }

  private void copyPublic(final Path from, final Path to, final Element declaration)
      throws MojoExecutionException {
    if (!Files.isDirectory(from)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(from)) {
      final List<Path> assets = new ArrayList<>();
      paths.filter(Files::isRegularFile).forEach(assets::add);
      for (final Path asset : assets) {
        String relative =
            from.relativize(asset).toString().replace(java.io.File.separatorChar, '/');
        if ((declaration.getElementsByTagName("include").getLength() == 0
                || matches(declaration, "include", relative))
            && !matches(declaration, "exclude", relative)) {
          copyInto(asset, to.resolve(relative).getParent());
        }
      }
    } catch (final IOException problem) {
      throw new MojoExecutionException("could not copy assets from " + from, problem);
    }
  }

  private boolean matches(Element declaration, String tag, String path) {
    NodeList patterns = declaration.getElementsByTagName(tag);
    for (int i = 0; i < patterns.getLength(); i++) {
      String glob = ((Element) patterns.item(i)).getAttribute("name");
      if (org.codehaus.plexus.util.SelectorUtils.matchPath(glob, path, true)) {
        return true;
      }
    }
    return false;
  }

  private void copyInto(final Path asset, final Path directory) throws MojoExecutionException {
    try {
      Files.createDirectories(directory);
      Files.copy(
          asset,
          directory.resolve(asset.getFileName()),
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.COPY_ATTRIBUTES);
    } catch (final IOException problem) {
      throw new MojoExecutionException("could not copy " + asset, problem);
    }
  }

  private void write(final Path file, final String content) throws MojoExecutionException {
    try {
      Files.createDirectories(file.getParent());
      Files.writeString(file, content, StandardCharsets.UTF_8);
    } catch (final IOException problem) {
      throw new MojoExecutionException("could not write " + file, problem);
    }
  }

  private Document parse(final Path path) throws MojoExecutionException {
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // A GWT module names a DTD that is not worth a network round trip, and that a
      // build has no business depending on being reachable.
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      final DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setEntityResolver(
          (publicId, systemId) -> new org.xml.sax.InputSource(new java.io.StringReader("")));
      return builder.parse(path.toFile());
    } catch (final Exception problem) {
      throw new MojoExecutionException("could not parse " + path, problem);
    }
  }

  private static String fileNameOf(final String path) {
    final int slash = path.lastIndexOf('/');
    return slash < 0 ? path : path.substring(slash + 1);
  }

  private static String shortName(final String className) {
    return className.substring(className.lastIndexOf('.') + 1);
  }
}
