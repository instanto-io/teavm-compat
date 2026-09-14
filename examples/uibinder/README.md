# Hello UiBinder on TeaVM

A standalone example of the shared UiBinder processor and GWT compatibility
runtime. It uses the compatibility library's basic widgets, with no Bootstrap or
Material dependency. A template creates a button and label; its Java handler
updates the label when the button is clicked.

Use JDK 21 and Maven. Install the matching `teavm-compat` artifacts first with
`mvn clean install` from the repository root. Then, from this directory:

```sh
mvn clean package
jwebserver -b 127.0.0.1 -p 8080 -d target/site
```

Open [the application](http://127.0.0.1:8080/) and press **Greet**.

Copy this directory to start another application. The POM is independent of the
compatibility reactor. Keep the Java owner and XML template in matching packages
under `src/main/java` and `src/main/resources`; Maven copies the template before
running the annotation processor.

See the [usage guide](../../gwt-uibinder-processor/README.md) and
[generation and linking design](../../docs/UIBINDER.md). The generated Java is in
`target/generated-sources/annotations`, and its provider descriptor is in
`target/classes/META-INF/services`. Deploy the complete `target/site` directory.
