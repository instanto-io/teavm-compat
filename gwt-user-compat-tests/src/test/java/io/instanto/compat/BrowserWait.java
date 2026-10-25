package io.instanto.compat;

import java.util.function.BooleanSupplier;
import org.teavm.jso.browser.Window;

/** Waits for observable browser events rather than assuming a machine-specific loading delay. */
final class BrowserWait {
  private BrowserWait() {}

  // TeaVM turns Thread.sleep into a cooperative browser timer. Yielding here allows DOM events
  // to run; the predicate and deadline make this a bounded condition wait, not a fixed test delay.
  @SuppressWarnings("java:S2925")
  static void until(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 5000;
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() >= deadline)
        throw new AssertionError("Browser condition timed out");
      Thread.sleep(1);
    }
  }

  static void nextEventLoopTurn() throws InterruptedException {
    boolean[] completed = {false};
    Window.setTimeout(() -> completed[0] = true, 0);
    until(() -> completed[0]);
  }
}
