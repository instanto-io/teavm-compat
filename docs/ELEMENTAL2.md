# Elemental2 on TeaVM

`io.instanto:elemental2-compat` is a standalone library providing access to browser
and JavaScript APIs from Java applications compiled with TeaVM. It generates
bindings for all eight Maven modules in Elemental2 1.2.3. Keep your application's
`elemental2.*` imports and replace the upstream Elemental2 dependencies with this
artifact. The [main README](../README.md) shows repository setup and BOM usage.

| Package | Browser APIs |
|---|---|
| `elemental2.core` | JavaScript objects, arrays, dates and typed arrays |
| `elemental2.dom` | Documents, elements, events and browser services |
| `elemental2.promise` | Promises |
| `elemental2.svg` | SVG elements, geometry, lengths and transforms |
| `elemental2.webstorage` | Local and session storage |
| `elemental2.indexeddb` | Databases, object stores, indexes, transactions and cursors |
| `elemental2.webgl` | WebGL graphics APIs |
| `elemental2.media` | Web Audio and encrypted-media APIs |

## Generation and coverage

Maven downloads the original 1.2.3 JARs, which include their Java declarations.
The generator translates every Java source file from all eight modules into
TeaVM bindings. The build compiles the whole result, and a coverage test checks
that upstream types, fields and method overloads have generated counterparts.
Native overlay casts use TeaVM's
inherited `JSObject.cast()` method.

Generated code lives under `target/compat/elemental2`. Its `sources.sha256` file
records the generated source hashes. Upstream archives and generated sources
are build inputs and outputs, rather than copies maintained in Git.

Generation coverage does not guarantee that every API is available in every
browser. Some declarations describe obsolete browser APIs. The existing
limitations for native constructor varargs and `Js.asConstructorFn(Class)` also
apply; see [the compatibility guidance](../README.md#what-compatibility-means).

Browser checks exercise SVG lengths, bounding boxes, path lengths and matrix
transforms; IndexedDB schema creation, indexed lookup, committed writes and
deletion; and offline audio rendering. These run with TeaVMTestRunner. The
supplementary browser suite checks WebGL pixel output with a software renderer,
because TeaVM's default headless Chrome launcher disables GPU support.

The IndexedDB test writes through Elemental2 and reads through TeaVM's own
`org.teavm.jso.indexeddb` API, verifying that both APIs can access the same browser
database.

## Versions

The compatibility artifact uses exactly the upstream version it targets:
Elemental2 1.2.3 is adapted as `io.instanto:elemental2-compat:1.2.3`. During
development its version is `1.2.3-SNAPSHOT`. There is no TeaVM revision suffix.
The separate group ID and artifact name identify the adaptation.

The parent, BOM and shared tools have their own versions. The BOM selects the
Elemental2 adaptation version explicitly. Other dependencies must not assume
that their version is the same as the Elemental2 version.

Before release, remove `-SNAPSHOT` from the library version and its references.
Advance the Elemental2 version only when changing the upstream compatibility
target. Released artifacts are immutable; development changes are tested in
snapshots before that release is published.
