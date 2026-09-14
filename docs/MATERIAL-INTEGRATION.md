# Material compatibility integration

This change integrates the Material port's runtime and generator work into the
Instanto compatibility repository. Consumers use normal `0.1.0-SNAPSHOT`
coordinates rather than private `material-SNAPSHOT` builds.

- `gwt-user-compat` gains Material's DOM, event, form, resource-loading, browser
  service and native-handle behaviour. Image load/error handlers and panel lifecycle
  ordering now support the original Material WebP and tabs examples. Text fields
  add initialisation events, the shared/client date-formatter hierarchy,
  suggestion selection and factory hooks, and synthetic key events with a
  relative element.
- `jsinterop-binding-generator` adapts supported JSNI and native GWT method
  boundaries. Namespaced methods use safe JavaScript parameter names and array
  application for varargs, including jQuery's `proxy(function, context, ...)`.
  Native method results declared as `Object` convert primitive values to their
  Java equivalents while preserving opaque native-object identity.
- `gwt-uibinder-processor` extracts the existing Bootstrap processor and includes
  the generic-setter, preformatted-text and default-string additions exercised
  by Material's original templates, plus subpackage-qualified widget names.
- `teavm-classlib-compat` provides the pinned TeaVM 0.15 exception workaround.
- `gwt-api` contains portable assertions; the optional native GWT test
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
mvn -Pnative-gwt-baseline -pl gwt-api-browser-baseline -am verify
```

The reactor passed 78 TeaVM browser checks, 21 UiBinder processor tests,
20 binding-generator tests and three browser reuse tests. The optional original
GWT reference passed 42 checks. The binding generator's reserved-name, varargs and native `Object` result
regressions are checked by its unit suite and real jQuery calls in the downstream
Material browser tests.

The Material consumer compiles the complete pinned core/jQuery Java source set
and runs 33 original showcase views on TeaVM, including text fields, error labels
navigation guidance and Media. This is
evidence for the tested paths, not full GWT or Material API parity. Selected
WebP, autocomplete, combo-box, input-mask and time-picker addins are included;
other addins, full-page navigation patterns and broader behavioural coverage
remain separate work. The table module now compiles all 189 pinned upstream Java
sources and runs six original table views. Seven Webapp Testkit scenarios exercise
their rendering and remounting, paging, sort events, data replacement, column
visibility, selection, density, grouping and infinite scrolling.

Table support adds typed live DOM rows and cells, number-formatting behaviour,
bitless events and native-handle equality and hashing to `gwt-user-compat`.
Portable assertions check these behaviours against GWT, including percent parsing
and using separately wrapped handles as map keys. UiBinder now reuses
`@UiField(provided = true)` instances and resolves handlers against the owner's
declared generic field types.

The Material generator registers the original jQuery callback declarations with
`NativeCallbackBindings`. Supported callbacks receive wrapped DOM handles and
boxed primitive arguments, and retain their native identity for handler removal.
Native DOM property getters use the same wrapping boundary. This does not imply
support for every native field, callback shape or GWT API. Consumers must recompile
against the updated bindings; previously compiled widget JARs may retain old
native method signatures.

Recompile consumers when updating the date-formatter hierarchy: the predefined
format enum now belongs to `com.google.gwt.i18n.shared.DateTimeFormat`, as in GWT.

Navigation coverage also requires scalar native `Object` arguments to unbox Java
primitive values through `Js.asAny`; otherwise a boxed toast duration becomes
`NaN` in JavaScript and never expires. Generator tests retain the existing
element-wise varargs conversion. Material checks native jQuery value types and
the original navbar toast expiry after mounting and remounting.

Media’s original fullscreen exit handler requires `Style.clearHeight()`,
`clearZIndex()`, `clearBottom()` and `clearLeft()`. These clear only their CSS
property. The shared style assertions exercise fullscreen cleanup and preservation
of unrelated styles against TeaVM and the optional original GWT reference.

Materialbox wraps an image in a placeholder before the original CodeSection
inserts its heading. `ComplexPanel.insert` now follows GWT's container-element
index semantics instead of inserting before the next logical widget's element,
which may no longer be a direct DOM child. The shared panel checks cover wrapped
children, intervening text nodes, reordering and removal on both backends.

The fullscreen button compares its original mixed-case label. GWT's
`Element.getInnerText()` uses `textContent`, whereas the browser's `innerText`
reflects CSS uppercase transformation. The shared element wrapper now reads and
writes `textContent`; portable assertions cover case, hidden text, literal
newlines and null clearing on an attached element.

The live Material showcase omits upstream screenshot galleries and external
demo links. Navbar retains its local inline examples; interactive full-page
navbar and side-navigation patterns remain to be ported. Upstream comparisons
are provided in the Material documentation.
