# TeaVM GWT Compatibility Contracts

Tests, not a library. This module produces an empty jar and nothing depends on it.

Its job is to run a shared set of assertions about GWT behaviour against
[`gwt-user-compat`](../gwt-user-compat), compiled by TeaVM and executed in Chrome.

## Why it is a module of its own

The assertions live in
[`gwt-api`](../gwt-api), and they are
run twice:

| against | by |
|---|---|
| the real `gwt-user` | [`gwt-user-jvm-contract-tests`](https://github.com/instanto-io/bootstrap-widgets/tree/main/gwt/gwt-user-jvm-contract-tests), [`gwt-bootstrap-widget-tests`](https://github.com/instanto-io/bootstrap-widgets/tree/main/gwt/gwt-bootstrap-widget-tests) |
| this compatibility layer | this module |

That is the point. A contract passing against GWT and failing here is a divergence,
and it is reported as a test failure naming the behaviour.

Portable Material assertions are compiled against each backend's API. The TeaVM
module copies their sources from `gwt-api`, so changes to GWT wrapper
hierarchies cannot be hidden by linking old compiled tests. TeaVM-specific native
fixtures live here and are not counted as upstream GWT parity checks.

## Running them

```
mvn -pl gwt-user-compat-tests -am test
```

They need Chrome. `teavm-junit` compiles the tests to JavaScript and runs them there,
so this exercises the compiled artefact rather than the JVM.

This repository does not contain the previously documented
`native-gwt-baseline` profile or `gwt-api-browser-baseline` module. Original-GWT
reference runs belong to the downstream widget repositories above. A passing
TeaVM test alone is not evidence of upstream GWT parity.

Generated-resource tests run javac's UiBinder processor, then load its providers
through `GWT.create` in the TeaVM browser. Lifecycle regressions exercise detach
failures, data-window bounds, handler cleanup, history and script reuse. Scheduling
and panel tests cover queue ordering, cancellation, capture, previews and ownership.

The TeaVM-specific tests also exercise canvas sizing, scaling and image export,
typed string-array factories and file-input creation. Currency tests cover
format/parse round trips, negative amounts, zero- and three-decimal currencies,
and invalid input. They use `gwt-modular-services-compat` and do not imply full
GWT number-pattern or canvas compatibility.
