# TeaVM compatibility libraries

This project provides TeaVM implementations of selected Java browser APIs from
Elemental2, JsInterop base and GWT. It also provides tools for GWT templates
and browser resources. The compatibility libraries keep familiar Java package
and class names, so code using supported APIs can be compiled by TeaVM without
rewriting its imports.

## Choose the library

| Artifact | Provides |
| --- | --- |
| `jsinterop-base-compat` | TeaVM implementations of `Js`, `JsPropertyMap`, and `JsArrayLike` |
| `elemental2-compat` | Generated TeaVM bindings for Elemental2 core, DOM, promises, SVG, storage, IndexedDB, WebGL and media APIs |
| `gwt-user-compat` | A TeaVM implementation of the supported legacy `com.google.gwt.*` and `com.google.web.bindery.*` client APIs |
| `gwt-modular-services-compat` | TeaVM implementations of the supported `org.gwtproject.safehtml`, `i18n`, and `editor` APIs |
| `gwt-uibinder-processor` | A javac annotation processor for supported UiBinder templates, ClientBundle resources, SafeHtmlTemplates and default string constants |
| `gwt-resources-compat-maven-plugin` | Packages widget CSS, scripts, fonts and images, generates code to load them, and copies them into an application's website folder |
| `jsinterop-binding-generator` | A build tool that adapts supported JsInterop declarations and JSNI bodies to TeaVM JSO |

`elemental2-compat` brings in `jsinterop-base-compat` automatically, and
`gwt-user-compat` brings in
[`instanto-teavm-classlib`](https://github.com/instanto-io/instanto-teavm), which
adds class-library methods TeaVM lacks while compiling.

See [Elemental2 coverage](docs/ELEMENTAL2.md) for the generated bindings,
including DOM, SVG, storage, IndexedDB, WebGL and media APIs. The
[UiBinder processor guide](gwt-uibinder-processor/README.md) and
[resource plugin guide](gwt-resources-compat-maven-plugin/README.md) show the
two build tools in use.

## Library ports

[teavm-charba](https://github.com/instanto-io/teavm-charba) adapts Charba core and
provides a six-example chart showcase. [Tealeaf](https://github.com/instanto-io/tealeaf)
maintains the gwty-leaflet Java API with a Leaflet 1.9.4 map demo. Each has its own
build, coordinates, tested-coverage notes and resource-ownership guide.

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

See [GWT compatibility coverage and ownership](docs/GWT-COMPATIBILITY.md) for the
supported porting APIs, resource lifetimes, scheduling boundaries and limits.

## Use UiBinder with TeaVM

The UiBinder processor runs during Java compilation. Put it on the
annotation-processor path and include your `.ui.xml` templates and bundle
resources in the compiler inputs. It generates providers that the compatibility
runtime finds through `GWT.create`. The
[processor usage guide](gwt-uibinder-processor/README.md) and
[standalone application](examples/uibinder) show a complete example.

The [generation and linking design](docs/UIBINDER.md) explains how the
providers are discovered and linked into the browser application.

### Browser coverage and Sonar

The verification workflow collects JVM coverage with JaCoCo and browser coverage
with Chromium's V8 execution counters. TeaVM's source maps map the executed and
missed JavaScript locations back to owned Java source lines. The browser report
is written to `target/browser-coverage.xml` and imported by Sonar alongside
JaCoCo; it reports line coverage, not inferred Java branch coverage. Third-party
sources are ignored, and collection fails if a compiled browser test fails or
no owned source locations can be mapped.

After `mvn clean install`, run `npm ci --prefix scripts/browser-coverage`,
`npm test --prefix scripts/browser-coverage`, and
`npm run collect --prefix scripts/browser-coverage`. Install the pinned Chromium
revision with `scripts/browser-coverage/node_modules/.bin/playwright install chromium`
if needed. Keep the coverage report from the same source revision as the analysis.
