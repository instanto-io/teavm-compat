# UiBinder on TeaVM

`gwt-uibinder-processor` generates Java implementations
of supported GWT UiBinder templates during javac compilation. Use it with
`gwt-user-compat` and retain your existing UiBinder interfaces and `.ui.xml` files.

## Use it in an application

Put the processor on javac's annotation-processor path and use it with
`gwt-user-compat`. The [standalone UiBinder example](../examples/uibinder)
shows a complete application using basic GWT-compatible widgets.

Place `GreetingView.java` in `src/main/java/example/uibinder/` and its template
`GreetingView.ui.xml` in `src/main/resources/example/uibinder/`. Maven copies the
template before javac runs. Use the owner class's name and package for the template,
and declare a nested interface extending `UiBinder` (its name is unrestricted):

```java
public class GreetingView extends Composite {
    interface Binder extends UiBinder<Widget, GreetingView> {}
    private static final Binder BINDER = GWT.create(Binder.class);

    @UiField Button greet;
    @UiField Label result;

    public GreetingView() {
        initWidget(BINDER.createAndBindUi(this));
    }

    @UiHandler("greet")
    void greet(ClickEvent event) {
        result.setText("Hello from UiBinder");
    }
}
```

The [complete Java file](../examples/uibinder/src/main/java/example/uibinder/GreetingView.java)
includes the imports. Fields and handlers are package-visible so the generated
implementation can access them. The template constructs those widgets:

```xml
<ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
             xmlns:g="urn:import:com.google.gwt.user.client.ui">
  <g:FlowPanel>
    <g:Button ui:field="greet" text="Greet"/>
    <g:Label ui:field="result" text="Ready"/>
  </g:FlowPanel>
</ui:UiBinder>
```

The launcher attaches `new GreetingView()` through `RootPanel.get().add(...)`.
The [example POM](../examples/uibinder/pom.xml) compiles that launcher with TeaVM
and copies an HTML host which loads `app.js` and calls `main()`.

To use another widget library, change the template's widget imports to its packages
and include its TeaVM artifact, assets and initialisation. The processor resolves
constructors, setters and handlers from the widget classes on javac's classpath.

## How the binding reaches the browser

javac discovers the processor through its service descriptor on the processor
path. The processor emits a Java implementation and a separate provider
descriptor for the application's `Binder`. TeaVM reads that provider descriptor
during compilation and includes the provider constructor and reachable binding
code in the generated application. In the browser, the compatibility runtime's
`GWT.create(Binder.class)` obtains the linked provider through `ServiceLoader`.

See [UiBinder generation and linking](../docs/UIBINDER.md) for the two discovery
steps, generated file names, provider precedence, packaging and troubleshooting.

## Supported features and limits

The processor supports widget construction, fields, handlers, setters (including
inherited generic setters), `@UiConstructor`, `@UiFactory`, typed styles, ClientBundle
text/CSS/image/data resources, SafeHtmlTemplates and annotated default string constants. It preserves whitespace in preformatted examples. Generated
providers participate in the compatibility runtime's `GWT.create` mechanism;
explicit application providers take precedence.

Fields marked `@UiField(provided = true)` must already contain an instance when
binding starts. The processor reuses that instance, including final fields, and
applies the template's setters, children and handlers. Generic event handlers are
resolved using the owner's declared field type, including inherited fields.

Put templates and bundle resources on the compilation classpath before javac runs.
Configure this artifact under Maven's `annotationProcessorPaths`, with annotation
processing enabled (`proc=full` on JDKs that require it). The processor is a build
dependency; it does not belong in the browser runtime.

This is a supported subset of GWT generation. Locale property bundles and locale
permutations require explicit providers; full GWT deferred binding, arbitrary
generators and every UiBinder feature are not implemented. Its compiler tests
exercise generated source and report unsupported templates at compilation time.

The owner convention is a top-level class with one nested interface directly
extending `UiBinder`. Annotate that interface with `@UiTemplate("Other.ui.xml")`
to override the filename; a leading `/` makes the path classpath-relative,
otherwise it is relative to the owner's package. Missing or malformed templates
are compilation errors. Multiple binders per owner and inherited binder interfaces
are not supported.

`@UiFactory` methods on the owner can construct widgets instead of their constructors.
Factory parameters consume matching XML attributes. A factory must be accessible
to the generated class; ambiguous factories are errors. `ui:with` supports provided
fields, constructible classes and interfaces obtained through `GWT.create`.
Expressions can chain zero-argument methods or fields, for example
`text="{resources.caption.getText}"` or `resource="{resources.icon}"`.

A `ui:style` may declare `field`, `type` (a `CssResource` interface) and `src`.
Class names are scoped using the owner's qualified name and style field, avoiding
collisions between owners with the same simple name in different packages.
CSS strings and whitespace are preserved. The generated style resource is shared
between bindings and injected once. `@external` selectors keep their names.

ClientBundle methods require `@Source`. Text and CSS may concatenate multiple
sources; images and data take one. Resources and their text/URI values are cached.
CSS class accessors support `@ClassName`; local CSS `url(...)` assets are embedded
relative to the stylesheet. CSS `@import` is rejected: resolve imports before
compilation. This is ordinary CSS plus `@external`, not the complete GWT CSS/GSS
language: constants, conditional rules, sprites, resource-function substitutions,
and escaped class identifiers are unsupported. `@NotStrict` is accepted as an
annotation but does not relax missing-accessor validation.

Images are embedded data URIs with intrinsic dimensions for PNG, JPEG, GIF and BMP;
SVG/WebP images need an explicit provider or a DataResource. There is no image
spriting, RTL flipping or external-resource size threshold. Data resources expose
GWT's `SafeUri` return type. Large resources should use the resource staging plugin
instead of increasing JavaScript bundle size. CSP must allow the generated data URIs
and injected styles used by the application.

SafeHtmlTemplates support text placeholders and quoted attribute values. Plain
values are HTML-escaped, `SafeHtml` is inserted only in text context, and whole-value
URI placeholders are sanitized (or accepted as `SafeUri`). Dynamic markup names,
unquoted attributes, SVG/MathML, script/style and other active-content elements, event-handler
attributes, `style`, `srcdoc`, and `srcset` placeholders are rejected. URI placeholders
must occupy the entire attribute value. Unsupported contexts fail during compilation;
this is deliberately a subset of GWT's template compiler.

`ui:msg`, `ui:image`, `ui:data`, `ui:attribute`, and widget-specific directives other
than `g:item` remain unsupported. Declare images/data in a ClientBundle and reference
them through `ui:with` instead.

## Examples in widget ports

The [Bootstrap Widgets](https://github.com/instanto-io/bootstrap-widgets) and
[Material Widgets](https://github.com/instanto-io/material-widgets) repositories
contain examples of applying this processor in their respective TeaVM ports.
