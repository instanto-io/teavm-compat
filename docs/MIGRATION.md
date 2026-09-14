# Consume the independent build

Import `io.instanto:teavm-compat-bom:0.1.0-SNAPSHOT` and add the package repository in
the README. Widget and compatibility releases have independent versions.

`elemental2-compat` now uses `1.2.3-SNAPSHOT`, matching its Elemental2 1.2.3
compatibility target. Import the updated BOM or change explicit references from
`0.1.0-SNAPSHOT`. The parent, BOM and build tools keep their own versions. The
expanded artifact includes IndexedDB, WebGL and media alongside the other five
Elemental2 modules; Java package names are unchanged. Run `mvn clean verify`
after changing versions to remove previously copied JARs from the build output.

| Former artifact | Replacement |
|---|---|
| `teavm-jsinterop-compat` | `jsinterop-base-compat` |
| `teavm-elemental2-compat` | `elemental2-compat` |
| `teavm-gwt-modular-services` | `gwt-modular-services-compat` |
| `teavm-gwt-compat` | `gwt-user-compat` |
| `bootstrap-widget-contracts` | `gwt-api` |
| `gwt-api-contracts` | `gwt-api` |
| `teavm-module-maven-plugin` | `gwt-resources-compat-maven-plugin` |

The resource plugin is now
`io.instanto:gwt-resources-compat-maven-plugin:0.1.0-SNAPSHOT`, with the project
name **GWT Resources Compatibility Maven Plugin**. Replace the old plugin name
and `1.0-SNAPSHOT` version in application and library POMs. Its `generate` and
`stage-assets` goals, Java packages and `META-INF/teavm-assets.properties` format
are unchanged. The old plugin is no longer built in Bootstrap Widgets.

For `generate`, set `resourcesClass` to your library's class providing `cssBase()`
and `jsBase()`. The plugin no longer assumes Bootstrap 5. Bootstrap's POMs now
set this explicitly so their generated code stays the same.
See the [plugin guide](../gwt-resources-compat-maven-plugin/README.md) for repository
setup and local installation. Maven BOM imports do not supply plugin versions;
declare the plugin version in your build.

The reusable `widget-processor` sources have also been extracted as
`io.instanto:gwt-uibinder-processor:0.1.0-SNAPSHOT`. Update Maven annotation
processor paths to use that artifact. Its Java processor package and service
entry remain unchanged. Material consumes the extracted artifact; existing
Bootstrap builds can migrate their processor dependency separately.

The shared `gwt-api` test artifact uses the neutral Java package
`io.instanto.gwt.testing.api` and GWT module
`io.instanto.gwt.testing.api.GwtApi`. Replace imports from
`io.instanto.gwt.testing.contracts` (or the earlier
`io.instanto.bootstrap.testing.contracts`) and replace the former
`GwtApiContracts` or `BootstrapWidgetContracts` module with `GwtApi`.
Update the Maven dependency to `io.instanto:gwt-api` and recompile the tests.
This change affects test dependencies, not widget runtime APIs.
Both the Bootstrap and Material reference suites use the same neutral package.

All groupIds remain `io.instanto`. Former runtime coordinates remain in the widget
repositories as relocation POMs, not duplicate implementations.

Domino Widgets is now TeaVM-only; its former GWT distribution is no longer built or
published. GWT users should use DominoKit upstream. Bootstrap retains both targets.
