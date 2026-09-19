# UiBinder on TeaVM

`io.instanto:gwt-uibinder-processor:0.1.0-SNAPSHOT` generates Java implementations
of supported GWT UiBinder templates during javac compilation. Use it with
`gwt-user-compat` and retain your existing UiBinder interfaces and `.ui.xml` files.

## Use it in an application

Use JDK 21 and Maven with the matching `teavm-compat` artifacts installed or
available from your configured Maven repository. Copy the
[standalone UiBinder example](../examples/uibinder) for a complete application.
It uses the basic widgets supplied by `gwt-user-compat`.

The application depends on `io.instanto:gwt-user-compat:0.1.0-SNAPSHOT` and
`org.teavm:teavm-classlib:0.15.0`. Add the processor to javac's processor path:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.14.1</version>
  <configuration>
    <release>17</release>
    <proc>full</proc>
    <annotationProcessorPaths>
      <path>
        <groupId>io.instanto</groupId>
        <artifactId>gwt-uibinder-processor</artifactId>
        <version>0.1.0-SNAPSHOT</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

Place `GreetingView.java` in `src/main/java/example/uibinder/` and its template
`GreetingView.ui.xml` in `src/main/resources/example/uibinder/`. Maven copies the
template before javac runs. Use the owner class's name and package for the template,
and declare a nested interface extending `UiBinder`. It can have any name; this
example uses `Binder`:

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

From the example directory, run `mvn clean package`, then
`jwebserver -b 127.0.0.1 -p 8080 -d target/site`. Open
[the application](http://127.0.0.1:8080/) and press **Greet**.

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
inherited generic setters), text-only ClientBundle resources and annotated default
string constants. It preserves whitespace in preformatted examples. Generated
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

The current owner convention is a top-level class with a nested interface named
`Binder` directly extending `UiBinder`; alternate interface names and
`@UiTemplate` path overrides are not supported. Typed `ui:style` interfaces and
`ui:msg`, `ui:image`, `ui:data` and `ui:attribute` are also unsupported. Keep the
matching template at the conventional resource path; a missing template currently
produces a compiler warning and no provider.

## Examples in widget ports

The [Bootstrap Widgets](https://github.com/instanto-io/bootstrap-widgets) and
[Material Widgets](https://github.com/instanto-io/material-widgets) repositories
contain examples of applying this processor in their respective TeaVM ports.
