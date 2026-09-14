# TeaVM compatibility libraries

This project provides TeaVM implementations of selected Java browser APIs from
Elemental2, JsInterop base, and GWT. It also provides shared build tools for using
GWT templates and browser resources with TeaVM.

The compatibility JARs use the original Java package and class names. Source
that imports those APIs can therefore be compiled by TeaVM without being
rewritten to TeaVM JSO types. At run time, the implementation delegates to
TeaVM's browser APIs or directly to JavaScript.

These are focused compatibility layers, not complete reimplementations of every
upstream library.

## Choose the library

| Artifact | Provides |
| --- | --- |
| `jsinterop-base-compat` | TeaVM implementations of `Js`, `JsPropertyMap`, and `JsArrayLike` |
| `elemental2-compat` | Generated TeaVM bindings for Elemental2 core, DOM, promises, SVG, storage, IndexedDB, WebGL and media APIs |
| `gwt-user-compat` | A TeaVM implementation of the supported legacy `com.google.gwt.*` and `com.google.web.bindery.*` client APIs |
| `gwt-modular-services-compat` | TeaVM implementations of the supported `org.gwtproject.safehtml`, `i18n`, and `editor` APIs |
| `gwt-uibinder-processor` | A javac annotation processor for supported UiBinder templates, ClientBundle text resources and default string constants |
| `gwt-resources-compat-maven-plugin` | Packages widget CSS, scripts, fonts and images, generates code to load them, and copies them into an application's website folder |
| `jsinterop-binding-generator` | A build tool that adapts supported JsInterop declarations and JSNI bodies to TeaVM JSO |
| `teavm-classlib-compat` | The TeaVM 0.15 exception workaround brought in by `gwt-user-compat` |

`elemental2-compat` brings in `jsinterop-base-compat` automatically.

Its version follows the upstream Elemental2 version: `1.2.3-SNAPSHOT` during
development, with `1.2.3` as the release version. The BOM manages this separately
from its own version. See [Elemental2 coverage and usage](docs/ELEMENTAL2.md).

The [resource plugin guide](gwt-resources-compat-maven-plugin/README.md) explains
the library and application build steps. Add it under Maven's `<plugins>`;
it runs during the build and is not an application dependency.

`gwt-user-compat` and `gwt-modular-services-compat` cover different package
names and can be used together. The first covers the packages historically
shipped in `gwt-user`; the second covers newer modular `org.gwtproject`
packages.

## Add a dependency

Artifacts are published from this repository's GitHub Packages registry:

```xml
<repository>
  <id>github-teavm-compat</id>
  <url>https://maven.pkg.github.com/instanto-io/teavm-compat</url>
</repository>
```

Add only the compatibility library your TeaVM module uses:

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>elemental2-compat</artifactId>
  <version>1.2.3-SNAPSHOT</version>
</dependency>
```

If a project uses several compatibility libraries, the optional BOM keeps their
versions aligned:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.instanto</groupId>
      <artifactId>teavm-compat-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

With the BOM imported, omit the version from individual
`io.instanto` compatibility dependencies.

## Use the compatibility JAR instead of the upstream JAR

A compatibility library and the upstream library it replaces define classes in
the same Java packages. Do not put both implementations on the TeaVM classpath.
Classpath order would decide which class TeaVM sees, producing fragile builds
and confusing linkage failures.

In particular:

- replace `com.google.elemental2:*` and `com.google.jsinterop:base` with
  `elemental2-compat` in a TeaVM module;
- replace `org.gwtproject:gwt-user` with `gwt-user-compat`; and
- exclude upstream SafeHtml, editor, or i18n implementations when using
  `gwt-modular-services-compat`.

`com.google.jsinterop:jsinterop-annotations` contains annotations rather than
a competing run-time implementation and is safe to retain.

Modules that are compiled with GWT or J2CL should continue to use the upstream
libraries. These compatibility artifacts are the TeaVM side of that dependency
choice.

## What compatibility means

The primary goal is source compatibility: application and library source keeps
its existing imports while the build selects a TeaVM implementation.

Coverage is driven by real TeaVM consumers and is intentionally incremental. A
class or method not present in a compatibility JAR is currently unsupported; it
is not an intentional statement that the upstream API should behave
differently.

The project includes browser contracts for the behavior it supports. Those
contracts compile with TeaVM and run in a browser, covering both API shape and
observable browser behavior.

UiBinder processing runs during Java compilation. Add the processor to your
TeaVM module's compiler configuration:

```xml
<annotationProcessorPaths>
  <path>
    <groupId>io.instanto</groupId>
    <artifactId>gwt-uibinder-processor</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </path>
</annotationProcessorPaths>
```

Copy `.ui.xml` templates and bundle resources into the compiler output directory
before compilation, as with Maven's normal `process-resources` phase. Generated
providers are discovered by the compatibility runtime's `GWT.create` support.
See the [processor usage guide](gwt-uibinder-processor/README.md) and
[standalone UiBinder application](examples/uibinder). The
[generation and linking design](docs/UIBINDER.md) explains javac discovery,
generated service descriptors, TeaVM's build-time linking and browser lookup.

Current build baselines are:

| Component | Version |
| --- | --- |
| TeaVM | 0.15.0 |
| Elemental2 inputs | 1.2.3 |
| legacy GWT API | 2.13.1 |
| JUnit contracts | 4.13.2 |

## How the project is built

Upstream binaries and source snapshots are not stored in this repository.
Versions are declared in the parent Maven build.

For generated Elemental2 bindings, Maven resolves the declared upstream
artifacts into the local repository and supplies their resolved paths to
`jsinterop-binding-generator`. The generator reads the Java declarations from
those JARs, converts their JsInterop annotations to TeaVM JSO annotations, and
writes generated sources under `target/compat/elemental2`.

The GWT compatibility modules contain maintained TeaVM implementations under
their own `src/main/java` trees. Those files are product code rather than
captures of upstream repositories.

No sibling checkout, downloaded source archive, or committed input JAR is
required.

## Build

Use JDK 21:

```bash
mvn clean verify
```

The browser contracts use Chrome by default. Set
`-Dteavm.junit.js.runner=browser-firefox` to use Firefox.

See [design notes](docs/DESIGN.md) and
[artifact migration](docs/MIGRATION.md) for repository-level details.

Development history is kept privately under `cstainton`; Instanto receives clean
release snapshots. See [the publishing workflow](docs/PUBLISHING.md).

## Shared build parent

For local builds, install the shared parent from a sibling `instanto-poms`
checkout with `mvn -f ../instanto-poms/pom.xml install`. Release instructions
are in `instanto-poms/RELEASING.md`.
