/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. No GWT source is included.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.google.gwt.core.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.*;
import org.teavm.jso.browser.Window;

/** GWT scheduling queues. Compatibility-owned browser callbacks enter through runEntry. */
public abstract class Scheduler {
  public interface ScheduledCommand {
    void execute();
  }

  public interface RepeatingCommand {
    boolean execute();
  }

  private static final Impl INSTANCE = new Impl();

  public static Scheduler get() {
    return INSTANCE;
  }

  public abstract void scheduleDeferred(ScheduledCommand command);

  public abstract void scheduleFinally(ScheduledCommand command);

  public abstract void scheduleFinally(RepeatingCommand command);

  public abstract void scheduleEntry(ScheduledCommand command);

  public abstract void scheduleEntry(RepeatingCommand command);

  public abstract void scheduleFixedDelay(RepeatingCommand command, int delayMs);

  public abstract void scheduleFixedPeriod(RepeatingCommand command, int delayMs);

  public abstract void scheduleIncremental(RepeatingCommand command);

  /** Adapter boundary for browser callbacks; nested entries share the outer finally queue. */
  public static void runEntry(Runnable callback) {
    INSTANCE.enter(callback);
  }

  private static final class Impl extends Scheduler {
    private final ArrayDeque<RepeatingCommand> entry = new ArrayDeque<>();
    private final ArrayDeque<RepeatingCommand> finallyQueue = new ArrayDeque<>();
    private int depth;
    private boolean fallbackPending;

    private void enter(Runnable callback) {
      boolean outer = depth++ == 0;
      try {
        if (outer) drain(entry);
        callback.run();
      } finally {
        try {
          if (outer) drain(finallyQueue);
        } finally {
          depth--;
        }
      }
    }

    // GWT dispatch must finish remaining callbacks/cleanup even after AssertionError,
    // then propagate every failure via UmbrellaException.
    @SuppressWarnings("java:S1181")
    private void drain(ArrayDeque<RepeatingCommand> queue) {
      if (queue.isEmpty()) return;
      List<RepeatingCommand> repeat = new ArrayList<>();
      java.util.Set<Throwable> failures = new java.util.LinkedHashSet<>();
      while (!queue.isEmpty()) {
        RepeatingCommand command = queue.remove();
        try {
          if (command.execute()) repeat.add(command);
        } catch (Throwable failure) {
          failures.add(failure);
        }
      }
      queue.addAll(repeat);
      if (!failures.isEmpty()) throw new com.google.gwt.event.shared.UmbrellaException(failures);
    }

    private void fallback() {
      if (depth != 0 || fallbackPending) return;
      fallbackPending = true;
      microtask(
          () -> {
            fallbackPending = false;
            enter(() -> {});
          });
    }

    @Override
    public void scheduleDeferred(ScheduledCommand command) {
      java.util.Objects.requireNonNull(command);
      Window.setTimeout(() -> enter(command::execute), 0);
    }

    @Override
    public void scheduleFinally(ScheduledCommand command) {
      java.util.Objects.requireNonNull(command);
      scheduleFinally(
          () -> {
            command.execute();
            return false;
          });
    }

    @Override
    public void scheduleFinally(RepeatingCommand command) {
      finallyQueue.add(java.util.Objects.requireNonNull(command));
      fallback();
    }

    @Override
    public void scheduleEntry(ScheduledCommand command) {
      java.util.Objects.requireNonNull(command);
      scheduleEntry(
          () -> {
            command.execute();
            return false;
          });
    }

    @Override
    public void scheduleEntry(RepeatingCommand command) {
      entry.add(java.util.Objects.requireNonNull(command));
      fallback();
    }

    @Override
    public void scheduleFixedDelay(RepeatingCommand command, int delayMs) {
      java.util.Objects.requireNonNull(command);
      if (delayMs < 0) throw new IllegalArgumentException("Negative delay");
      Window.setTimeout(
          () ->
              enter(
                  () -> {
                    if (command.execute()) scheduleFixedDelay(command, delayMs);
                  }),
          delayMs);
    }

    @Override
    public void scheduleFixedPeriod(RepeatingCommand command, int periodMs) {
      java.util.Objects.requireNonNull(command);
      if (periodMs <= 0) throw new IllegalArgumentException("Nonpositive period");
      int[] id = new int[1];
      id[0] =
          Window.setInterval(
              () ->
                  enter(
                      () -> {
                        boolean executed = false;
                        try {
                          if (!command.execute()) Window.clearInterval(id[0]);
                          executed = true;
                        } finally {
                          if (!executed) Window.clearInterval(id[0]);
                        }
                      }),
              periodMs);
    }

    @Override
    public void scheduleIncremental(RepeatingCommand command) {
      java.util.Objects.requireNonNull(command);
      scheduleDeferred(
          () -> {
            long until = System.currentTimeMillis() + 8;
            for (int i = 0; i < 100; i++) {
              if (!command.execute()) return;
              if (System.currentTimeMillis() >= until) break;
            }
            scheduleIncremental(command);
          });
    }

    @JSBody(params = "action", script = "Promise.resolve().then(action);")
    private static native void microtask(Action action);
  }

  @JSFunctor
  private interface Action extends JSObject {
    void run();
  }
}
