package com.google.gwt.animation.client;

import com.google.gwt.dom.client.Element;

/** Time-based animation. A canceled/completed run releases its scheduled frame. */
public abstract class Animation {
  private final AnimationScheduler scheduler;
  private AnimationScheduler.AnimationHandle handle;
  private boolean running;
  private boolean started;
  private boolean canceledAfterStart;
  private int duration;
  private double start;
  private Element element;
  private int generation;

  public Animation() {
    this(AnimationScheduler.get());
  }

  protected Animation(AnimationScheduler scheduler) {
    this.scheduler = java.util.Objects.requireNonNull(scheduler);
  }

  public void run(int duration) {
    run(duration, System.currentTimeMillis(), null);
  }

  public void run(int duration, double startTime) {
    run(duration, startTime, null);
  }

  public void run(int duration, Element element) {
    run(duration, System.currentTimeMillis(), element);
  }

  public void run(int duration, double startTime, Element element) {
    if (duration < 0) throw new IllegalArgumentException("Negative duration");
    cancel();
    this.duration = duration;
    this.start = startTime;
    this.element = element;
    running = true;
    started = false;
    int run = ++generation;
    update(System.currentTimeMillis(), run);
  }

  public void cancel() {
    if (!running) return;
    canceledAfterStart = started;
    stop();
    onCancel();
  }

  public boolean isRunning() {
    return running;
  }

  private void stop() {
    running = false;
    generation++;
    if (handle != null) {
      handle.cancel();
      handle = null;
    }
    element = null;
  }

  private void update(double now, int run) {
    if (!running || generation != run) return;
    handle = null;
    try {
      if (now >= start) {
        if (!started) {
          started = true;
          onStart();
        }
        if (!running || generation != run) return;
        double progress = duration == 0 ? 1 : Math.min(1, (now - start) / duration);
        if (progress >= 1) {
          stop();
          onComplete();
          return;
        }
        onUpdate(interpolate(progress));
      }
      if (running && generation == run)
        handle = scheduler.requestAnimationFrame(timestamp -> update(timestamp, run), element);
    } catch (RuntimeException | Error failure) {
      if (generation == run) stop();
      throw failure;
    }
  }

  protected double interpolate(double progress) {
    return (1 + Math.cos(Math.PI + progress * Math.PI)) / 2;
  }

  protected void onStart() {
    onUpdate(interpolate(0));
  }

  protected void onComplete() {
    onUpdate(interpolate(1));
  }

  protected void onCancel() {
    if (canceledAfterStart) onComplete();
  }

  protected abstract void onUpdate(double progress);
}
