# Consume the independent build

Import `io.instanto:teavm-compat-bom:0.1.0-SNAPSHOT` and add the package repository in
the README. Widget and compatibility releases have independent versions.

| Former artifact | Replacement |
|---|---|
| `teavm-jsinterop-compat` | `jsinterop-base-compat` |
| `teavm-elemental2-compat` | `elemental2-compat` |
| `teavm-gwt-modular-services` | `gwt-modular-services-compat` |
| `teavm-gwt-compat` | `gwt-user-compat` |
| `bootstrap-widget-contracts` | `gwt-api-contracts` |

All groupIds remain `io.instanto`. Former runtime coordinates remain in the widget
repositories as relocation POMs, not duplicate implementations.

Domino Widgets is now TeaVM-only; its former GWT distribution is no longer built or
published. GWT users should use DominoKit upstream. Bootstrap retains both targets.
