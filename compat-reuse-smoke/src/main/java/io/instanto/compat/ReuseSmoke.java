package io.instanto.compat;

import elemental2.dom.DomGlobal;
import org.dominokit.domino.logger.ConsoleLoggerAdapter;

/** Exercises a second published library without any widget dependencies. */
public final class ReuseSmoke {
  public static void main(String[] args) {
    BindingContracts.run();
    DomGlobal.document.body.appendChild(BrowserApis.render());
    new ConsoleLoggerAdapter("compat-reuse").info("compat-reuse-success");
    DomGlobal.document.body.setAttribute("data-reuse", "ready");
  }
}
