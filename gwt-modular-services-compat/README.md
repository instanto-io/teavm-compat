# GWT modular services for TeaVM

This library provides the `org.gwtproject` service APIs used by applications compiled
with TeaVM. Add `io.instanto:gwt-modular-services-compat` to your Maven dependencies,
using the version selected by the compatibility BOM.

## Currency formatting

Use an ISO 4217 code to select the currency:

```java
NumberFormat money = NumberFormat.getCurrencyFormat("EUR");
String displayed = money.format(1234.50);
double amount = money.parse(displayed);
```

Import `org.gwtproject.i18n.client.NumberFormat`. Formatting uses the browser's
internationalisation API and locale. Parsing accepts the currency text, grouping
and decimal separators emitted by that formatter. Currency symbols and spacing
vary with the locale; do not assume a fixed display string.

The current tests cover positive and negative USD, EUR, GBP and OMR values, JPY
rounding, OMR's three decimal places, and invalid codes and input. This is not a
complete implementation of GWT number patterns or locale permutations.
