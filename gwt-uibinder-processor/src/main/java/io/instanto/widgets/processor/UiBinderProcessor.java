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
package io.instanto.widgets.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Turns a {@code .ui.xml} template into the Java a UiBinder implementation would be.
 *
 * <p>GWT answers {@code GWT.create(SomeBinder.class)} with a generator reached through deferred
 * binding. This processor replaces the supported generation steps at javac time and registers their
 * ordinary Java implementations as ServiceLoader providers for the compatibility GWT.create. It
 * does not implement GWT deferred-binding rules or TeaVM compiler plugins.
 *
 * <p>That last part matters. A UiBinder generator has to know whether a widget has a
 * {@code @UiConstructor} and what its parameters are, what type each setter takes, and whether that
 * type is an enum, following the hierarchy because a widget inherits most of its setters. Those are
 * questions about types, and {@code Elements} and {@code Types} answer them exactly.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class UiBinderProcessor extends AbstractProcessor {

  private static final String UI_NS = "urn:ui:com.google.gwt.uibinder";
  private static final String IMPORT_PREFIX = "urn:import:";
  private static final String BINDER = "com.google.gwt.uibinder.client.UiBinder";

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment round) {
    if (round.processingOver()) {
      return false;
    }
    for (final Element element : round.getRootElements()) {
      new ResourceBundleGenerator(processingEnv).process(element);
      new SafeHtmlTemplateGenerator(processingEnv).process(element);
      new StringConstantsGenerator(processingEnv).process(element);
      if (element.getKind() != ElementKind.CLASS) {
        continue;
      }
      final TypeElement owner = (TypeElement) element;
      final TypeElement binder = findBinder(owner);
      if (binder != null) {
        try {
          generate(owner, binder);
        } catch (final Failure failure) {
          processingEnv
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.ERROR,
                  failure.getMessage(),
                  failure.element == null ? owner : failure.element);
        } catch (final Exception problem) {
          processingEnv
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.ERROR,
                  "could not generate a UiBinder for " + owner.getSimpleName() + ": " + problem,
                  owner);
        }
      }
    }
    return false;
  }

  /** The nested interface that extends UiBinder, if the owner declares one. */
  private TypeElement findBinder(final TypeElement owner) {
    for (final Element nested : owner.getEnclosedElements()) {
      if (nested.getKind() != ElementKind.INTERFACE) {
        continue;
      }
      final TypeElement candidate = (TypeElement) nested;
      for (final TypeMirror parent : candidate.getInterfaces()) {
        if (processingEnv.getTypeUtils().erasure(parent).toString().equals(BINDER)) {
          return candidate;
        }
      }
    }
    return null;
  }

  static final class Failure extends RuntimeException {
    final Element element;

    Failure(final String message, final Element element) {
      super(message);
      this.element = element;
    }
  }

  private void generate(final TypeElement owner, final TypeElement binder) throws IOException {
    final String pkg =
        processingEnv.getElementUtils().getPackageOf(owner).getQualifiedName().toString();
    final String simple = owner.getSimpleName().toString();
    final Document template = readTemplate(pkg, simple, owner, binder);
    if (template == null) {
      return;
    }

    final Emitter emitter = new Emitter(processingEnv, owner);
    emitter.readStyles(template.getDocumentElement());
    emitter.readWith(template.getDocumentElement());
    rejectUnsupported(template.getDocumentElement(), owner);
    final Node root = firstElement(template.getDocumentElement());
    if (root == null) {
      throw new Failure(simple + ".ui.xml has no widget in it", owner);
    }
    final String rootVar = emitter.emit(root, null);

    final String impl = simple + "_BinderImpl";

    // Assembled whole before anything is created, because handlerWiring() can still
    // fail: a file opened and then abandoned half-written draws a second "reached end
    // of file" error from javac, which buries the diagnostic that actually explains it.
    final StringBuilder source = new StringBuilder();
    source.append("// Generated from ").append(simple).append(".ui.xml. Do not edit.\n");
    source.append("package ").append(pkg).append(";\n\n");
    source.append("/** UiBinder implementation for {@link ").append(simple).append("}. */\n");
    source
        .append("public class ")
        .append(impl)
        .append(" implements ")
        .append(simple)
        .append(".")
        .append(binder.getSimpleName())
        .append(" {\n\n");
    source.append(emitter.styleDeclarations());
    source.append("    @Override\n");
    source
        .append("    public ")
        .append(emitter.rootType())
        .append(" createAndBindUi(final ")
        .append(simple)
        .append(" owner) {\n");
    source.append(emitter.styleInitialization());
    source.append(emitter.body());
    source.append(emitter.fieldAssignments());
    source.append(emitter.handlerWiring());
    source.append("        return ").append(rootVar).append(";\n");
    source.append("    }\n}\n");

    final JavaFileObject file = processingEnv.getFiler().createSourceFile(pkg + "." + impl, owner);
    try (Writer out = file.openWriter()) {
      out.write(source.toString());
    }

    // so the compatibility layer's GWT.create finds it without deferred binding
    // ServiceLoader looks the interface up by its binary name, so a nested
    // interface is Owner$Binder rather than Owner.Binder. Getting this wrong
    // leaves GWT.create unable to find its generated provider.
    final String binderBinary = processingEnv.getElementUtils().getBinaryName(binder).toString();
    final FileObject service =
        processingEnv
            .getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + binderBinary);
    try (Writer out = service.openWriter()) {
      out.write(pkg + "." + impl + "\n");
    }
  }

  /**
   * A parser that never reaches the network.
   *
   * <p>Every template opens with a DOCTYPE pointing at dl.google.com, which GWT does not fetch and
   * neither should a build: resolving it would put a network call in the middle of compilation,
   * failing offline and slowing it down otherwise. The entity is not needed to read the document,
   * so external entities are refused outright, which also closes the usual XXE exposure.
   */
  private javax.xml.parsers.DocumentBuilder parser()
      throws javax.xml.parsers.ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(
        (publicId, systemId) -> new org.xml.sax.InputSource(new java.io.StringReader("")));
    return builder;
  }

  private Document readTemplate(
      final String pkg, final String simple, final Element owner, final TypeElement binder) {
    String name =
        SourceSupport.annotation(binder, "com.google.gwt.uibinder.client.UiTemplate", "value");
    if (name == null) name = simple + ".ui.xml";
    final String resourcePackage = name.startsWith("/") ? "" : pkg;
    if (name.startsWith("/")) name = name.substring(1);
    for (final StandardLocation where :
        new StandardLocation[] {
          StandardLocation.SOURCE_PATH, StandardLocation.CLASS_PATH, StandardLocation.CLASS_OUTPUT
        }) {
      final InputStream stream;
      try {
        stream =
            processingEnv.getFiler().getResource(where, resourcePackage, name).openInputStream();
      } catch (final IOException missing) {
        // A missing resource may be available at the next location.
        continue;
      }
      try (stream) {
        return parser().parse(stream);
      } catch (final IOException
          | org.xml.sax.SAXException
          | javax.xml.parsers.ParserConfigurationException failure) {
        throw new Failure("Cannot parse " + name + ": " + failure.getMessage(), owner);
      }
    }
    processingEnv
        .getMessager()
        .printMessage(Diagnostic.Kind.ERROR, "no " + name + " found for " + simple, owner);
    return null;
  }

  /**
   * Rejects any ui: element this generator does not implement.
   *
   * <p>GWT's UiBinder has a wider vocabulary than this: ui:msg for internationalised text, ui:image
   * and ui:data for bundled resources, ui:attribute. These directives are unsupported; the
   * processor reports an error instead of silently omitting them from the generated UI.
   */
  private void rejectUnsupported(final Node root, final TypeElement owner) {
    final NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);
      if (child.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      if (Emitter.UI_NS.equals(child.getNamespaceURI())) {
        final String name = child.getLocalName();
        if (!"style".equals(name) && !"with".equals(name)) {
          throw new Failure("<ui:" + name + "> is not supported by this generator", owner);
        }
      }
      rejectUnsupported(child, owner);
    }
  }

  static Node firstElement(final Node parent) {
    final NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (UI_NS.equals(child.getNamespaceURI())) {
          final Node inner = firstElement(child);
          if (inner != null) {
            return inner;
          }
          continue;
        }
        return child;
      }
    }
    return null;
  }
}
