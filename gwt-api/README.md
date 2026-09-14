# Shared GWT API tests

`io.instanto:gwt-api` contains portable assertions about the GWT APIs
used by TeaVM widget ports. Bootstrap and Material consume this single test
artifact; neither widget library owns its package namespace.

Java assertions live in `io.instanto.gwt.testing.api`. Native GWT test
modules inherit `io.instanto.gwt.testing.api.GwtApi`. The JAR
includes Java sources for GWT compilation and compiled classes for JVM and
TeaVM test consumers.

Material-prefixed classes group the behaviours encountered while porting Material.
They test shared GWT contracts rather than replacing Material's own widget tests.
The runtime classification annotation identifies assertions needing real browser
semantics.

When updating from `io.instanto:gwt-api-contracts`, change the dependency to
`io.instanto:gwt-api`. Replace imports from `io.instanto.gwt.testing.contracts`
(or the earlier `io.instanto.bootstrap.testing.contracts`) with
`io.instanto.gwt.testing.api`, update the inherited GWT module to `GwtApi`, and
recompile the tests. The assertion class names and behaviour are unchanged.
