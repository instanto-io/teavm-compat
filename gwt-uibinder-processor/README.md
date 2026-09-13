# UiBinder on TeaVM

`io.instanto:gwt-uibinder-processor:0.1.0-SNAPSHOT` generates Java implementations
of supported GWT UiBinder templates during javac compilation. Use it with
`gwt-user-compat` and retain your existing UiBinder interfaces and `.ui.xml` files.

This is the processor originally developed for Bootstrap Widgets, extracted so
widget libraries and applications can depend on it directly. Material's templates
added coverage to the same implementation.

The processor supports widget construction, fields, handlers, setters (including
inherited generic setters), text-only ClientBundle resources and annotated default
string constants. It preserves whitespace in preformatted examples. Generated
providers participate in the compatibility runtime's `GWT.create` mechanism;
explicit application providers take precedence.

Put templates and bundle resources on the compilation classpath before javac runs.
Configure this artifact under Maven's `annotationProcessorPaths`, with annotation
processing enabled (`proc=full` on JDKs that require it). The processor is a build
dependency; it does not belong in the browser runtime.

This is a supported subset of GWT generation. Locale property bundles and locale
permutations require explicit providers; full GWT deferred binding, arbitrary
generators and every UiBinder feature are not implemented. Its compiler tests
exercise generated source and report unsupported templates at compilation time.
