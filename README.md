# TeaVM compatibility libraries

Use familiar Java browser APIs — Elemental2, JsInterop base, and GWT — in a TeaVM
application.

TeaVM has its own browser bindings (JSO) and does not understand JsInterop
annotations. Code written against Elemental2 or GWT therefore does not compile under
TeaVM. These libraries republish those APIs, under their original package names,
implemented against JSO. Existing code compiles unchanged; the classpath decides
which implementation it gets.

This is an independent project under `io.instanto`, not an official Google, GWT or
TeaVM distribution. Original Java packages and attribution are retained; see
[NOTICE](NOTICE).

## 1. Choose the artifacts you need

| Artifact | Replaces | Use it when your code imports |
| --- | --- | --- |
| `elemental2-compat` | Elemental2 1.2.3 | `elemental2.dom`, `.core`, `.svg`, `.promise`, `.webstorage` |
| `jsinterop-base-compat` | `jsinterop-base` | `jsinterop.base.Js`, `JsPropertyMap`, `JsArrayLike` |
| `gwt-user-compat` | `gwt-user` (client subset) | `com.google.gwt.*`, `com.google.web.bindery.*` |
| `gwt-modular-services-compat` | the `org.gwtproject` modules | `org.gwtproject.safehtml`, `.i18n`, `.editor` |

`elemental2-compat` brings JsInterop base in automatically.

### What "modular services" means

GWT 2 shipped one large `gwt-user` jar. The GWT project later split parts of it into
standalone, J2CL-compatible libraries published under the `org.gwtproject` groupId —
`gwt-safehtml`, `gwt-i18n`, `gwt-editor` and others. Those are the modules this
artifact covers, and it is named for them rather than for any one of them.

They are a different lineage from `com.google.gwt`, not a newer version of it: the
packages differ, and an application may legitimately use both. `gwt-user-compat`
covers the old packages; `gwt-modular-services-compat` covers the new ones.

Everything here is a subset — enough for the applications driving this work, not a
claim of complete upstream compatibility.

## 2. What it is compatible with

Every input is pinned by version and checksum in
[`upstream/bindings-lock.json`](upstream/bindings-lock.json) and
[`upstream/provenance.json`](upstream/provenance.json). The build verifies those
checksums, so the bindings cannot drift without the change being visible.

| Upstream | Pinned at |
| --- | --- |
| Elemental2 | **1.2.3** — core, dom, promise, svg, webstorage |
| GWT | **2.13.1** API level, for the `com.google.gwt` subset |
| TeaVM | **0.15.0** |
| JDK | **21** |

The Elemental2 jars are vendored under `upstream/` and checked:

| Artifact | SHA-256 |
| --- | --- |
| `com.google.elemental2:elemental2-core:1.2.3` | `e76b5bb0b13c…` |
| `com.google.elemental2:elemental2-dom:1.2.3` | `bc17f3c057f7…` |
| `com.google.elemental2:elemental2-svg:1.2.3` | `b1b8ce2fbaca…` |
| `com.google.elemental2:elemental2-webstorage:1.2.3` | `86b0b185e06a…` |
| `com.google.elemental2:elemental2-promise:1.2.3` | `ca0dc3c374be…` |

The `org.gwtproject` module sources are pinned differently, and it is worth being
precise about it. They are not taken from a published `org.gwtproject` release: they
are a subset extracted from [a recorded fork](https://github.com/cstainton/domino-ui)
at commit `31404a554cda`, with the archive's own checksum recorded alongside. So this artifact tracks the API level that
fork uses, not a version number you can look up upstream.

Nothing here claims complete upstream compatibility. Each artifact covers the surface
the driving applications need. If a class or member you use is missing, that is a gap
to fill rather than a deliberate exclusion — see chapter 6 for how the generated ones
are widened.

## 3. Add the dependencies

Use JDK 21 and TeaVM 0.15.0. Add the package repository and authenticate the
`github-teavm-compat` server in your Maven settings with a token that can read
packages:

```xml
<repository>
  <id>github-teavm-compat</id>
  <url>https://maven.pkg.github.com/instanto-io/teavm-compat</url>
</repository>
```

Import the BOM so versions stay aligned:

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

Then declare only what you need:

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>elemental2-compat</artifactId>
</dependency>
```

## 4. Remove the upstream artifacts

This is the rule that matters most. A compat artifact and the library it replaces
declare the same Java packages, so both on one classpath is undefined: which class
wins depends on order, and the failure is confusing.

Remove `com.google.elemental2:*` from any module that depends on
`elemental2-compat`. The build enforces this for its own modules with a
`backend-boundary` enforcer rule, and the same rule is worth adding downstream.

`gwt-user-compat` and `gwt-modular-services-compat` cover different packages from
each other, so they coexist. The GWT client adapter uses the official JsInterop
annotations artifact, which is annotations only and safe to keep.

An application that still targets GWT or J2CL should depend on the upstream
libraries directly. These adapters are for TeaVM.

## 5. Build and verify

```sh
mvn clean verify
```

JDK 21 and Chrome are required. The build formats maintained Java, verifies
checksums of the immutable inputs, runs the transformer tests and the
`TeaVMTestRunner` browser contracts, and gates on SpotBugs findings. Use
`-Dteavm.junit.js.runner=browser-firefox` for the Firefox contracts, and
`mvn -Pproduction clean verify` to enable advanced optimisation for the reuse app.

No widget checkout is needed. `compat-reuse-smoke` exercises the published Domino
console logger through the compatibility APIs, and the browser contracts drive one
shared document through both the GWT client and Elemental2 APIs.

## 6. How the bindings are produced

Most of this library is generated rather than written.

`jsinterop-binding-generator` reads the upstream artifacts and rewrites their
JsInterop annotations into TeaVM's equivalents — `@JsType` becomes `@JSClass`,
`@JsProperty` becomes `@JSProperty`, `@JsFunction` becomes `@JSBody` — preserving
package names, signatures and JS names.

| Source | Input | Output |
| --- | --- | --- |
| Elemental2 | the five upstream jars, pinned at 1.2.3 | `target/compat/elemental2` |
| GWT modules | pinned sources under `upstream/modular-services` | `target/compat/services` |

The generated sources become each module's source directory, so the jars contain
compiled bindings and no checked-in transcription. Widening coverage means adding an
input, not writing classes by hand.

Publication is a manually triggered GitHub Actions workflow, after verification.

See [design and provenance](docs/DESIGN.md) and [artifact migration](docs/MIGRATION.md).
