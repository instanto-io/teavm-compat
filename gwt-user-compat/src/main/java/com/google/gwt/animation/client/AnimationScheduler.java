package com.google.gwt.animation.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import org.teavm.jso.*;

/** Native animation frames with explicitly cancellable callbacks. */
public abstract class AnimationScheduler {
  public interface AnimationCallback {
    void execute(double timestamp);
  }

  public abstract static class AnimationHandle {
    public abstract void cancel();
  }

  private static final AnimationScheduler INSTANCE =
      new AnimationScheduler() {
        public AnimationHandle requestAnimationFrame(AnimationCallback callback, Element element) {
          java.util.Objects.requireNonNull(callback);
          return new Handle(callback);
        }
      };

  public static AnimationScheduler get() {
    return INSTANCE;
  }

  public AnimationHandle requestAnimationFrame(AnimationCallback callback) {
    return requestAnimationFrame(callback, null);
  }

  public abstract AnimationHandle requestAnimationFrame(
      AnimationCallback callback, Element element);

  private static final class Handle extends AnimationHandle {
    private AnimationCallback callback;
    private final int id;

    Handle(AnimationCallback callback) {
      this.callback = callback;
      id =
          request(
              timestamp -> {
                AnimationCallback pending = this.callback;
                this.callback = null;
                if (pending != null) Scheduler.runEntry(() -> pending.execute(timestamp));
              });
    }

    @Override
    public void cancel() {
      cancelFrame(id);
      callback = null;
    }
  }

  @JSFunctor
  private interface Frame extends JSObject {
    void execute(double timestamp);
  }

  @JSBody(
      params = "callback",
      script = "return window.requestAnimationFrame(function(){callback(Date.now());});")
  private static native int request(Frame callback);

  @JSBody(params = "id", script = "window.cancelAnimationFrame(id);")
  private static native void cancelFrame(int id);
}
