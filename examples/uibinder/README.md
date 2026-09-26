# Hello UiBinder on TeaVM

A standalone example of the shared UiBinder processor and GWT compatibility
runtime. It uses the compatibility library's basic widgets, with no Bootstrap or
Material dependency. A template creates a button and label; its Java handler
updates the label when the button is clicked.

Open [the Java view](src/main/java/example/uibinder/GreetingView.java) and
[its template](src/main/resources/example/uibinder/GreetingView.ui.xml).
The view binds a button and label; pressing **Greet** updates the label.

Copy this directory to start another application. The POM is independent of the
compatibility reactor. Keep the Java owner and XML template in matching packages
under `src/main/java` and `src/main/resources`; Maven copies the template before
running the annotation processor.

See the [usage guide](../../gwt-uibinder-processor/README.md) and
[generation and linking design](../../docs/UIBINDER.md).
