# Compatibility below widget libraries

The parent and BOM have independent versions. Runtime JARs depend on TeaVM and the
APIs they adapt, never on a widget library or JavaParser. The separate
`jsinterop-binding-generator` build tool accepts an output directory followed by
explicit source directories or JARs and writes deterministic source hash manifests.
Widget-specific input selection stays in each widget repository.

Elemental2 input coordinates and versions are declared in the parent POM. Maven
resolves those artifacts and exposes their local paths to the generator; upstream
JARs and source snapshots are not committed to this repository. The modular GWT and
GWT client modules contain maintained TeaVM implementations rather than copies of
upstream binaries. Existing contract Java packages and module names remain compatible
with native GWT reference test consumers.

All eight Elemental2 1.2.3 Maven modules are generated, including IndexedDB,
WebGL and media. The adapter's version matches its upstream target; the BOM
selects it independently of the parent and tools. See [Elemental2](ELEMENTAL2.md)
for generation checks, browser coverage and the version policy.

Seven duplicate JsInterop annotation declarations were replaced by the official
annotations dependency. A mixed browser contract caught `Js.asString` mishandling
already-Java strings from property maps; that conversion now preserves them.

Spotless applies formatting at validate. Immutable inputs are excluded. SpotBugs
rejects high-priority findings, with narrowly named exceptions for GWT's public
`Element`, `EventBus` and `UmbrellaException` compatibility aliases. Each deliberately
extends an implementation with the same simple class name in another GWT package.

The `gwt-uibinder-processor` was extracted from the processor developed for Bootstrap
Widgets. It remains a javac annotation processor rather than a runtime dependency.
Material's templates extend its coverage with inherited generic setters, preserved
preformatted text and default string constants; both widget libraries can use the
same processor. Import provenance is recorded under `provenance/`.
See [UiBinder generation and linking](UIBINDER.md) for the processor and provider
discovery steps, application packaging and `GWT.create` resolution in the browser.

`gwt-resources-compat-maven-plugin` provides the shared resource build steps.
Its `generate` goal reads GWT resource declarations and writes loading code;
`stage-assets` copies the browser files listed by libraries into the application's
website folder. The generated code uses `ScriptModule` from `gwt-user-compat`.
Library-specific paths and resource classes are supplied by the library build.
The plugin's tests create their own input files and need no widget checkout.

Material's compatibility contributions live below the widget layer: native GWT
handles are wrapped at generated JSO boundaries, DOM attributes and primary styles
retain GWT semantics, and browser services use native typed arrays, frames, media
elements and geolocation. The generator rejects unsupported JSNI Java-member
references and Java receiver access instead of silently emitting invalid code.
Portable contracts remain in `gwt-api`; TeaVM-specific browser fixtures
and regressions live in `gwt-user-compat-tests`.

`teavm-classlib-compat` applies the TeaVM 0.15 suppressed-exception initialisation
workaround during compilation. It is transitive from `gwt-user-compat`, pending
adoption of an upstream TeaVM release containing the fix.

These adapters do not certify every upstream API. Named contracts and widget
consumer tests provide evidence for the behavior they exercise.
