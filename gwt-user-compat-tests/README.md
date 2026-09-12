# TeaVM GWT Compatibility Contracts

Tests, not a library. This module produces an empty jar and nothing depends on it.

Its job is to run a shared set of assertions about GWT behaviour against
[`gwt-user-compat`](../gwt-user-compat), compiled by TeaVM and executed in Chrome.

## Why it is a module of its own

The assertions live in
[`gwt-api-contracts`](../gwt-api-contracts), and they are
run twice:

| against | by |
|---|---|
| the real `gwt-user` | [`gwt-user-jvm-contract-tests`](https://github.com/cstainton/bootstrap-widgets), [`gwt-bootstrap-widget-tests`](https://github.com/cstainton/bootstrap-widgets) |
| this compatibility layer | this module |

That is the point. A contract passing against GWT and failing here is a divergence,
and it is reported as a test failure naming the behaviour.

The shared assertions are compiled once against `gwt-user` and linked against the
compatibility layer, so they can only use API whose signatures match exactly in both.
Where they do not, a test is written once per backend instead, as the root panel
caching and detach tests are.

## Running them

```
mvn -pl gwt-user-compat-tests test
```

They need Chrome. `teavm-junit` compiles the tests to JavaScript and runs them there,
so this exercises the compiled artefact rather than the JVM.
