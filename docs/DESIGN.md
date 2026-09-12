# Compatibility below widget libraries

The parent and BOM have independent versions. Runtime JARs depend on TeaVM and the
APIs they adapt, never on a widget library or JavaParser. The separate
`jsinterop-binding-generator` build tool accepts an output directory followed by
explicit source directories or JARs and writes deterministic source hash manifests.
Widget-specific input selection stays in each widget repository.

Immutable Elemental2 JARs and an extracted modular service subset live in `upstream/`.
Source commits, archive provenance and per-file hashes are in `upstream/provenance.json`;
the build verifies them. No Domino source archive or sibling checkout is required.
GWT client emulation and portable contracts were extracted from the recorded
Bootstrap commit. Existing contract Java packages and module names remain compatible
with native GWT reference test consumers.

Seven duplicate JsInterop annotation declarations were replaced by the official
annotations dependency. A mixed browser contract caught `Js.asString` mishandling
already-Java strings from property maps; that conversion now preserves them.

Spotless applies formatting at validate. Immutable inputs are excluded. SpotBugs
rejects high-priority findings with one explicit exception: GWT's legacy
`com.google.gwt.user.client.Element` intentionally extends a DOM class named `Element`.

These adapters do not certify every upstream API. Named contracts and widget
consumer tests provide evidence for the behavior they exercise.
