# Material compatibility integration

This change integrates the Material port's runtime and generator work into the
Instanto compatibility repository. Consumers use normal `0.1.0-SNAPSHOT`
coordinates rather than private `material-SNAPSHOT` builds.

- `gwt-user-compat` gains Material's DOM, event, form, resource-loading, browser
  service and native-handle contracts.
- `jsinterop-binding-generator` adapts supported JSNI and native GWT method
  boundaries. Namespaced methods use safe JavaScript parameter names and array
  application for varargs, including jQuery's `proxy(function, context, ...)`.
- `gwt-uibinder-processor` extracts the existing Bootstrap processor and includes
  the generic-setter, preformatted-text and default-string additions exercised
  by Material's original templates.
- `teavm-classlib-compat` provides the pinned TeaVM 0.15 exception workaround.
- `gwt-api-contracts` contains portable assertions; the optional native GWT test
  application checks their reference behaviour.

The integration preserves the source-intake migration already in the checkout:
Elemental2 inputs are resolved by Maven and modular GWT implementations live in
their module's source tree. The removed vendored Elemental2 JARs were compared
with the Maven-resolved replacements: all five are byte-identical. The relocated
Intl bindings now use TeaVM annotations, with date/number formatting and parts
checked in Chromium, Firefox and WebKit.

Validation on JDK 21:

```sh
mvn clean install
mvn -Pnative-gwt-baseline -pl gwt-api-contracts-browser-baseline -am verify
```

The clean reactor passed 70 TeaVM browser contracts, 18 UiBinder processor tests
and three browser reuse tests. The optional original GWT reference passed 34
contracts. The binding generator's reserved-name/varargs regression was then
verified with its unit suite and real jQuery callbacks in the downstream Material
browser tests.

The Material consumer compiles the complete pinned core/jQuery Java source set
and runs 21 original showcase views on TeaVM. This is evidence for the tested
paths, not full GWT or Material API parity. Addins, tables, additional showcase
pages and broader behavioural coverage remain separate work.
