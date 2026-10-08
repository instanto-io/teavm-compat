# TeaVM classlib support has moved

General TeaVM class-library additions and regressions are maintained in
[Instanto TeaVM](https://github.com/instanto-io/instanto-teavm), using
`io.instanto:instanto-teavm-classlib:0.1.0-SNAPSHOT` and TeaVM 0.16.0.

This module now builds a relocation POM only. The former suppressed-exception
repair applied to TeaVM 0.15; 0.16 already fixes it. Its source is preserved in
Instanto TeaVM's `legacy/teavm-0.15` directory and is not compiled or registered.
Existing GWT event-bus tests remain in this repository; the shared classlib also
tests suppressed exceptions. GWT, Elemental and JSInterop adaptation continues
to live in `teavm-compat`.

Publish the shared artifact before merging and publishing this relocation.
Consumers pinned to TeaVM 0.15 must keep their previous compatibility artifact
until they upgrade.
