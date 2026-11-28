# GWT resources compatibility for TeaVM

`gwt-resources-compat-maven-plugin` prepares the CSS, JavaScript, fonts and images that
widgets need in the browser. It handles two parts of the build:

- **Building a widget library:** `generate` reads the library's GWT module files
  and ClientBundle declarations to find its browser files. It collects those
  files for packaging in the library JAR and generates Java code that loads the
  required stylesheets and scripts, telling the application when they are ready.
- **Building an application:** `stage-assets` copies those files from the library
  JARs into the folder your web server serves. Each library supplies a small
  configuration file that says where its files belong.

A library packages its stylesheets, scripts and fonts once. An application
then stages those files beside its compiled code and HTML page, ready for a
web server to serve.

If you are using a widget library, start with **Stage assets for an application**
below. If you maintain a library, see
[Include browser files in a library](#include-browser-files-in-a-library) and
[Generate resource loaders for a library](#generate-resource-loaders-for-a-library).

## Stage assets for an application

The `stage-assets` goal copies the files needed by your application's widget
libraries into the folder your web server serves. Each library includes a file
named `META-INF/teavm-assets.properties` telling the plugin which files to copy
and where to put them. For example, it can contain:

```properties
source=META-INF/example-assets
target=assets/example
```

When the widget library is built, Maven includes this file in its JAR alongside
the CSS, scripts and fonts. Later, when your application is built, `stage-assets`
reads it and copies the contents of `source` into `target` inside your website
folder. With the default settings, that means `target/site/assets/example/`.
Application authors do not need to create this file.

The plugin checks the libraries Maven uses to build and run your application,
including their dependencies. It can read JARs and compiled library folders from
the same Maven build.

Add this to your application's POM, using the plugin version selected for your build:

```xml
<plugin>
  <groupId>io.instanto</groupId>
  <artifactId>gwt-resources-compat-maven-plugin</artifactId>
  <version>YOUR_PLUGIN_VERSION</version>
  <executions>
    <execution><goals><goal>stage-assets</goal></goals></execution>
  </executions>
  <configuration>
    <outputDirectory>${project.build.directory}/site</outputDirectory>
  </configuration>
</plugin>
```

Running `mvn package` copies the files during Maven's `prepare-package` step.
The default destination is `target/site`. Put the compiled application and its
HTML page in the same folder. Set the destination to the website folder your server will use.

Tell your library where the browser can find its staged files before starting
it. For example, if the browser loads `assets/example/` beside the host page,
set that as the library's asset base.

The goal prepares files on disk. Your deployment process uploads them or makes
the folder available through your web server. Maven's `deploy` command publishes
Java packages to a Maven repository; use `package` to prepare this website.

The plugin keeps the folder structure intact so stylesheets can still find their
fonts and images. It replaces existing copies of the library files and leaves
other application files alone. It does not remove old files left by a library
upgrade. Use `mvn clean package` for a fresh `target/site`, or an empty destination
folder for a fresh deployment.

If two libraries supply different files for the same destination, the build
stops before copying. Identical copies are accepted. The build also stops if a
library lists a folder that is missing or empty, or if none of the libraries has
an asset configuration file. Paths must stay inside the destination folder;
symbolic links inside that folder are rejected.

## Include browser files in a library

Library authors keep the file at
`src/main/resources/META-INF/teavm-assets.properties`. Maven includes it in the
JAR when it builds the library. For example:

```properties
source=META-INF/example-assets
target=assets/example
```

`source` is the folder inside the JAR containing the browser files. `target` is
where those files should go inside the website. Both paths must name a folder
and be relative: they cannot start with `/` or contain `..`.

The plugin copies files from the named source folder. It skips libraries without
this configuration file. Once a library supplies the file, applications can use
`stage-assets` without writing their own copying instructions. Rebuild and install
older library JARs to include this file before using the goal.

## Generate resource loaders for a library

The `generate` goal reads the shared GWT sources, packages their browser files
into the TeaVM library JAR and generates Java code to load them.

- `<script>` and `<stylesheet>` entries name files to load.
- `<public>` entries name folders to copy, with rules for including or excluding
  files. Their folder structure is preserved.
- ClientBundle interfaces name files using `@Source`. The annotation can contain
  one filename or a list of filenames. The Java filename need not end in
  `ClientBundle`.
- JavaScript files from these bundles are added to the list of scripts to load.
  Other files are copied for the application to use.

The generated code uses `ScriptModule` to load scripts in order and tell the
application when they are ready. The optional `scriptPresence` setting names a
Java method that checks whether the page has already loaded a script.

The application needs `gwt-user-compat`, which supplies `ScriptModule`. Set the
plugin's `resourcesClass` to your library's Java class with static `cssBase()` and
`jsBase()` methods returning the browser addresses for its CSS and scripts.
This setting belongs to the library's build. The existing `idPrefix` setting
is still accepted for compatibility with older build configurations.

`assetRoot` sets the folder used during development. `packagedRoot` sets where
the same files go inside the library JAR. Applications can then copy them using
`stage-assets` and set the browser address as described above. TeaVMTestRunner
can also serve files directly from a JAR through `/resources/`.

Use `inlineTextBundles` for ClientBundle interfaces whose `TextResource` methods
must return text immediately. The plugin generates Java implementations and the
registration files used to find them. Each method must name a single file with
`@Source`; the file's contents are preserved. Resource objects and reconstructed text
are cached, so repeated accessor calls do not allocate another wrapper or rebuild the string.

Scripts in these bundles are packaged for the application to load when needed.
For example, an editor can load the language selected by the user. Large text
files are split into Java string chunks to fit Java's class-file limits.

The plugin does not rewrite CSS, combine images into sprites or implement GWT's
rules for choosing generated implementations. Applications must also choose
compatible versions when several libraries need the same browser script.

## Compile a GWT library's own source with TeaVM

A widget library shared between GWT and TeaVM can keep one source. Write its browser
calls with JsInterop, and `adapt-sources` rewrites them for TeaVM before the TeaVM module
compiles: native `@JsType`s become TeaVM JSO types, `@JsFunction`s become functors, and GWT
DOM wrappers are passed to JavaScript as the DOM nodes they stand for.

```xml
<plugin>
  <groupId>io.instanto</groupId>
  <artifactId>gwt-resources-compat-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>adapt-shared-sources</id>
      <goals>
        <goal>adapt-sources</goal>
      </goals>
      <configuration>
        <sourceRoot>${project.build.directory}/shared-sources</sourceRoot>
        <outputRoot>${project.build.directory}/teavm-sources</outputRoot>
      </configuration>
    </execution>
  </executions>
</plugin>
```

It runs in `process-sources` and writes only `.java` files, so compile `outputRoot` (for
example with `build-helper-maven-plugin`'s `add-source`) instead of `sourceRoot`.

Older code that still uses JSNI can set `<legacyJsni>true</legacyJsni>`: JSNI bodies become
`@JSBody` first. A body that calls into Java through `@Class::member` cannot be translated,
so the build fails naming the file; give that callback a `@JsFunction` type and call it as a
function instead. Java 17 source syntax, including records, is accepted.

### Boxed numbers and booleans

GWT passes a `Double`, `Number` or `Boolean` to JavaScript as a plain number or boolean, so
JsInterop libraries often declare native members with these types. TeaVM would pass the Java
object instead, and JavaScript would receive something that is not a number.

The adapter converts them on native methods of native `@JsType` classes and interfaces,
including `@JsProperty` accessors and static methods. The Java signature stays the same:

```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public interface PointsMaterialParameters {
  @JsProperty void setSize(Number size); // JavaScript receives 0.03, not a Java Double
  @JsProperty Number getSize();          // a number comes back as a Double
}
```

- Arguments of type `Double`, `Float`, `Integer`, `Short`, `Byte` and `Number` cross as
  JavaScript numbers, and `Boolean` as a JavaScript boolean. `null` stays `null`.
- Results come back as the declared box. A `Number` result is a `Double`, as in GWT. A
  `null` or `undefined` result is `null`.
- Arrays of these boxes, such as `Float[]`, and varargs such as `Number...` cross as
  JavaScript arrays of numbers or booleans, converted element by element. An array result
  such as `Double[]` comes back as a new Java array of boxes, with `undefined` elements as
  `null`. As with TeaVM's primitive arrays, JavaScript gets a copy, so changes it makes to
  an argument array are not seen in Java.
- `Long` and `Character` are left alone, because GWT does not pass them as primitives
  either. A library type with the same simple name as a box, such as its own `Number`, is
  left alone too.

Native fields cannot be converted this way. A field is read and written in the code that
uses it, not in the adapted declaration, so there is no place to put the conversion, and
changing the field's Java type would change what callers may assign to it. The adapter
therefore fails on a boxed native field and names it, for example
`Native field demo.PointsMaterialParameters.size is declared Number`. Declare such a field
`double` or `boolean` (or `float[]`, `boolean[]` and so on for an array), or replace it
with `@JsProperty` accessor methods. Fields marked `@JsOverlay` are Java-only and are not
affected.

Native constructors cannot be converted either. TeaVM turns `new PolarGridHelper(...)`
straight into a JavaScript `new` in the calling code and passes each argument by its
declared type; the constructor's own body never runs. So the adapter fails on a native
constructor with a boxed or boxed-array parameter and names it, for example
`Native constructor demo.PolarGridHelper(double, double, double, double, Color, Number)
declares parameter color2 as Number`. Declare the parameter with the matching primitive
type. Where the boxed parameter allowed `null` to mean "leave it out", add an overload
without that parameter.

Methods convert one-dimensional arrays only. A native member declared with a deeper array
of boxes, such as `Double[][]`, is rejected by name in the same way.

Direct subclasses of GWT `JavaScriptObject` with empty no-argument constructors
can remain in the canonical sources. The adapter generates TeaVM wrapper
constructors and converts native arguments and results, including nulls. Native
instance operations should delegate to static JSNI helpers with an explicit
receiver argument. Nested overlays and stateful constructors are rejected;
arbitrary overlay casts and inherited overlay hierarchies are not implemented.
`@JsFunction` callbacks passed to JSNI helpers receive the same GWT-object
conversion as callbacks passed to JsInterop methods.

JsInterop `name` and `namespace` attributes can use string constants from the
adapted source set, including imported constants and string concatenation.
`JsPackage.GLOBAL` is recognized directly. Unresolved constant expressions fail
generation instead of producing a binding to an incorrect JavaScript name.
The [Charba core port](https://github.com/instanto-io/teavm-charba) exercises this path with upstream sources.

The adapter also preserves concrete `@JsFunction` values passed through generic
native void setters, such as `CallbackProxy<Callback>.setCallback(...)`, on
fields, parameters and local variables. The bridge keeps the native function
identity and converts erased object results. Overloaded/inherited setters and
arbitrary receiver expressions need an explicit bridge.

Native adaptation also resolves raw callback type parameters to their declared
upper bound and annotates implicit native JsInterop methods for TeaVM. These
paths are exercised by the [Tealeaf port](https://github.com/instanto-io/tealeaf),
including Java event callbacks and methods inherited through native interfaces.

Unbounded generic native results use the same conversion as `Object`, including converting an
absent JavaScript value to Java `null`. When a native class implements an interface declaring the
same getter, the generated conversion is inherited from that interface. This preserves the Java
interface relationship without overriding a TeaVM overlay implementation. Generic property
getters retain their original JavaScript property names. The Elemental2 collection browser tests
cover missing entries, stored Java object identity, strings and iterator result properties.
