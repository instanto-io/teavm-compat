# TeaVM GWT Compatibility

An implementation of the parts of `com.google.gwt.*` that the Bootstrap widgets use,
written against TeaVM's browser APIs.

You do not normally depend on this directly. Depend on
[`teavm-bootstrap3`](https://github.com/cstainton/bootstrap-widgets) or
[`teavm-bootstrap5`](https://github.com/cstainton/bootstrap-widgets) and this comes with it.

## What it is for

GWT widget code is written against `RootPanel`, `Widget`, `Element`, `HandlerManager`
and so on. Those classes are part of the GWT compiler's own library, so they are not
available when TeaVM compiles the same source. This module supplies them.

That is the whole trick behind the TeaVM port: one set of widget sources, two
compilers, and this layer standing where `gwt-user` stands on the other side.

## What it covers

Enough for the widgets, not all of GWT. Roughly:

- `com.google.gwt.user.client.ui` — the widget and panel hierarchy, `RootPanel`,
  `Composite`, `UIObject`
- `com.google.gwt.dom.client` — `Document`, `Element` and the typed elements
- `com.google.gwt.event` — the handler and event machinery
- `com.google.gwt.core.client` — `GWT`, `Scheduler`, `ScriptInjector`,
  `JavaScriptObject`
- `com.google.gwt.safehtml`, `com.google.gwt.i18n`, parts of `cellview`

`GWT.create` is the interesting one. GWT resolves it by deferred binding at compile
time; here it resolves through `ServiceLoader`, and a generated service file registers
the implementation. That is how UiBinder templates work on this backend without a GWT
generator.

## Where it differs from GWT

Deliberately, in places, and those differences are the reason the contract tests
exist. Two worth knowing if you read the source:

- `UIObject.getElement()` returns `com.google.gwt.dom.client.Element`. GWT returns the
  deprecated `com.google.gwt.user.client.Element`.
- `Node.appendChild` is not generic here.

Neither matters for code compiled from source against this layer, which is how the
widgets are built. They matter only for bytecode compiled against `gwt-user` and then
linked against this — which is exactly what the contract tests are.

## Keeping it honest

[`gwt-user-compat-tests`](../gwt-user-compat-tests) runs a shared set of
assertions about GWT behaviour against this implementation. The same assertions run
against the real `gwt-user` in
[`gwt-bootstrap-widget-tests`](https://github.com/cstainton/bootstrap-widgets) and
[`gwt-user-jvm-contract-tests`](https://github.com/cstainton/bootstrap-widgets). A contract
that passes on one and fails on the other is a divergence, and it is reported as one.

Adding a contract is how you record a piece of GWT behaviour the widgets have come to
depend on. That is more useful than it sounds: several real defects in this layer were
found that way, including `Composite` never telling the widget it wraps that it had
been attached, so nothing below one ever started.
