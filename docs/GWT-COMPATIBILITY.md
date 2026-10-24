# GWT compatibility for library ports

This layer implements a tested subset of GWT APIs over TeaVM. It does not run the
GWT compiler or its arbitrary deferred-binding generators. Replace upstream runtime
JARs in the TeaVM module; recompile ported libraries against these APIs.

## Porting coverage

| Area | Implemented here | Important limits |
| --- | --- | --- |
| Generated HTML/resources | SafeHtmlTemplates; cached ClientBundle TextResource, CssResource, ImageResource and DataResource providers | Supported HTML contexts and CSS syntax are described in the [processor guide](../gwt-uibinder-processor/README.md); no full GSS, sprites or runtime locale permutations |
| Core composition/events | FocusPanel, AbsolutePanel, HorizontalPanel, VerticalPanel; cell sizing/alignment; native preview, mouse capture; PopupPanel auto-hide/modal preview | No full layout-panel framework, popup glass/animation or GWT browser-quirk implementations; capture covers mouse events, not pointer capture |
| UiBinder | @UiTemplate, @UiFactory, provided fields, typed styles, chained ui:with resource references, generated provider discovery | One direct nested binder per top-level owner; unsupported UI directives fail compilation |
| Scheduling/animation | entry/finally/deferred, repeating entry/finally, fixed delay/period, incremental commands, Timer, AnimationScheduler and Animation | External raw JsInterop callbacks need the entry boundary below for exact queue ordering; browser frame timing is not deterministic |

HTTP/JSON expansion, RPC, full deferred binding, and a complete widget/layout
catalog remain separate work. API presence alone does not establish behavioral
parity: consult browser contracts for exercised behavior.

## Ownership and cleanup

Java/JavaScript garbage collection owns allocation and reclamation; there is no
manual `free`. Cleanup removes references held by panels, static registries,
browser listeners, queues and pending callbacks so objects can become collectible.

- Removing a widget detaches it, clears its parent, removes its DOM node and
  releases mouse capture. Cleanup continues when unload handlers throw; failures
  are reported after the affected ownership structures are cleaned up.
- DOM event sinks keep their Java listener only while the widget is active.
  Reattachment reinstalls dispatch. User handler registrations remain on the
  widget for reuse; remove registrations to stop individual subscriptions.
- PopupPanel.hide detaches the popup and unregisters preview/history listeners.
  Partner elements remain owned by the popup until explicitly removed or the popup
  itself becomes collectible. Hidden popups can be shown again.
- AbstractDataProvider removes its range subscription when a display is removed
  and rejects duplicate displays. Visible row storage is bounded by the visible
  range; off-page updates do not allocate gaps, and shrinking exact row counts
  discard stale data. Cell views currently rerender the visible page on refresh;
  they do not implement GWT's full deferred rendering/diff machinery.
- Default FormPanel submissions target an owned hidden iframe. Detach removes
  the frame and its listeners; attach recreates it. Explicit targets use the named
  browsing context instead. Submit completion is available for the owned frame;
  cross-origin response contents are unavailable and reported as null.
- RootPanel removes replaced roots from both its root cache and detach registry.
  Document-wide event-preview listeners exist only while previews/capture need them.
- ScriptModule clears waiters and native load/error listeners after completion or
  failure, and notifies all waiters even if one throws. `addReadyHandler` supplies
  a removable subscription; ordinary `whenReady` waits until load settles.
- Generated bundle resources and UiBinder styles are intentionally shared for the
  application lifetime. CSS injection is idempotent; rebinding does not accumulate
  style elements. Text and URI strings are cached rather than rebuilt per accessor.
- Cancel timers/animations owned by a widget in its unload/disposal path. Animation
  cancellation and completion release pending frames and element references.
  Scheduler repeating commands finish by returning false.

ScriptModule treats an existing host-owned script as already loaded. Supply a
presence predicate and start after the host has loaded it; browsers cannot reliably
recover a historical load event from an arbitrary script element. Concurrent loads
created by ScriptModule share load state. Stylesheets preserve insertion order,
but module readiness does not wait for CSS downloads.

## Scheduling boundaries

Compatibility-owned DOM callbacks, timers, history, forms and animation frames enter
through `Scheduler.runEntry`. Entry commands run before the outer callback; finally
commands run after it and all nested entries, before deferred commands. Commands
added to a draining queue are also drained; repeating entry/finally commands wait
for another entry. Fixed-delay schedules after execution, fixed-period uses the
browser interval, and incremental work yields after a bounded batch.

When exposing a callback directly through JsInterop, wrap its Java work:

```java
Scheduler.runEntry(() -> updateModelAndWidgets());
```

Scheduling entry/finally work outside a managed entry uses a microtask fallback.
That keeps work from being stranded but cannot reproduce every GWT `$entry` boundary
in arbitrary third-party JavaScript. AnimationScheduler timestamps use epoch
milliseconds, matching Animation.run's start-time argument.

## Validation

Use JDK 21 and Chrome:

```sh
mvn -pl gwt-uibinder-processor,gwt-resources-compat-maven-plugin,gwt-user-compat-tests -am test
```

The browser suite includes generated provider loading, resource caching and escaping,
scheduler ordering, cancellation, native event routing, and lifecycle regressions.
Compiler tests check generated source and actionable diagnostics. These tests establish
observable behavior; they are not heap profiling or proof that all consumers release
all references. Original-GWT parity requires the downstream reference suites described
in the [test-module guide](../gwt-user-compat-tests/README.md).

The independent reuse smoke application also runs in Chromium, Firefox and WebKit.
On a host where only some engines can launch, select them explicitly without
changing the default three-engine suite:

```sh
mvn -pl compat-reuse-smoke -am verify -Dcompat.reuse.browsers=chromium,webkit
```

A selected-engine run does not establish coverage for the omitted engines.
