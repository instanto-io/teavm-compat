# TeaVM class library compatibility

A compiler plugin for the pinned TeaVM 0.15.0 class library. `gwt-user-compat`
brings it in automatically; it requires no application initialization.

The plugin restores the empty suppressed-exception array omitted by TeaVM's
replacement `Throwable` constructors. This permits GWT's event bus to retain
multiple handler failures without crashing in `Throwable.addSuppressed`.
Constructors that already assign the field are left alone.

TeaVM discovers the plugin through `META-INF/services`. Its dependency on
`teavm-core` is provided by the compiler and is not a transitive application
dependency. [Shared browser contracts](../gwt-user-compat-tests/README.md) exercise
the repair and compare the observable behavior with GWT.

Review this workaround when changing TeaVM versions.
