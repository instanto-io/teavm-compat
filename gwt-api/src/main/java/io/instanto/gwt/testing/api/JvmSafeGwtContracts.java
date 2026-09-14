package io.instanto.gwt.testing.api;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Shared contracts that are proven to execute on the JVM with real gwt-user. */
public final class JvmSafeGwtContracts {
  private JvmSafeGwtContracts() {}

  @CompatibilityContract(ContractRuntime.JVM_SAFE)
  public static void safeHtmlEscapesAndPreservesTrustedFragments() {
    String dangerous = "<&>\"'";
    ContractAssertions.equal(
        "&lt;&amp;&gt;&quot;&#39;",
        SafeHtmlUtils.fromString(dangerous).asString(),
        "SafeHtmlUtils must escape HTML metacharacters");

    String result =
        new SafeHtmlBuilder()
            .appendHtmlConstant("<strong>")
            .appendEscaped(dangerous)
            .appendHtmlConstant("</strong>")
            .toSafeHtml()
            .asString();
    ContractAssertions.equal(
        "<strong>&lt;&amp;&gt;&quot;&#39;</strong>",
        result,
        "SafeHtmlBuilder must preserve only explicitly trusted fragments");
  }

  @CompatibilityContract(ContractRuntime.JVM_SAFE)
  public static void handlerManagerPreservesOrderAndSource() {
    Object source = new Object();
    HandlerManager manager = new HandlerManager(source);
    List<String> calls = new ArrayList<>();
    manager.addHandler(
        RecordingEvent.TYPE,
        event -> {
          ContractAssertions.same(source, event.getSource(), "first handler event source");
          calls.add("first");
        });
    manager.addHandler(
        RecordingEvent.TYPE,
        event -> {
          ContractAssertions.same(source, event.getSource(), "second handler event source");
          calls.add("second");
        });

    manager.fireEvent(new RecordingEvent());
    ContractAssertions.equal(
        Arrays.asList("first", "second"), calls, "handlers must run in registration order");
  }

  @CompatibilityContract(ContractRuntime.JVM_SAFE)
  public static void handlerMutationIsDeferredUntilTheNextDispatch() {
    HandlerManager manager = new HandlerManager(new Object());
    List<String> calls = new ArrayList<>();
    HandlerRegistration[] secondRegistration = new HandlerRegistration[1];
    manager.addHandler(
        RecordingEvent.TYPE,
        event -> {
          calls.add("first");
          secondRegistration[0].removeHandler();
          manager.addHandler(RecordingEvent.TYPE, later -> calls.add("late"));
        });
    secondRegistration[0] = manager.addHandler(RecordingEvent.TYPE, event -> calls.add("second"));

    manager.fireEvent(new RecordingEvent());
    ContractAssertions.equal(
        Arrays.asList("first", "second"), calls, "dispatch must use a stable handler snapshot");

    calls.clear();
    manager.fireEvent(new RecordingEvent());
    ContractAssertions.equal(
        Arrays.asList("first", "late"), calls, "mutations must apply to the next dispatch");
  }

  @CompatibilityContract(ContractRuntime.JVM_SAFE)
  public static void removedHandlerDoesNotReceiveLaterEvents() {
    HandlerManager manager = new HandlerManager(new Object());
    int[] calls = {0};
    HandlerRegistration registration = manager.addHandler(RecordingEvent.TYPE, event -> calls[0]++);
    manager.fireEvent(new RecordingEvent());
    registration.removeHandler();
    manager.fireEvent(new RecordingEvent());
    ContractAssertions.equal(1, calls[0], "removed handler must not receive a later event");
    ContractAssertions.equal(
        0, manager.getHandlerCount(RecordingEvent.TYPE), "handler count must reflect removal");
  }

  @CompatibilityContract(ContractRuntime.JVM_SAFE)
  public static void valueChangesRespectExplicitEventSuppression() {
    TestValue value = new TestValue();
    List<Boolean> observed = new ArrayList<>();
    value.addValueChangeHandler(
        event -> {
          ContractAssertions.same(value, event.getSource(), "value event source");
          observed.add(event.getValue());
        });

    value.setValue(true, false);
    value.setValue(false, true);
    value.setValue(false, true);
    ContractAssertions.equal(
        Arrays.asList(false), observed, "only a requested, changed value must emit an event");
  }

  private interface RecordingHandler extends EventHandler {
    void onRecording(RecordingEvent event);
  }

  private static final class RecordingEvent extends GwtEvent<RecordingHandler> {
    private static final Type<RecordingHandler> TYPE = new Type<>();

    @Override
    public Type<RecordingHandler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(RecordingHandler handler) {
      handler.onRecording(this);
    }
  }

  private static final class TestValue implements HasValue<Boolean>, HasHandlers {
    private final HandlerManager handlers = new HandlerManager(this);
    private boolean value;

    @Override
    public Boolean getValue() {
      return value;
    }

    @Override
    public void setValue(Boolean value) {
      setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
      boolean old = this.value;
      this.value = value != null && value;
      if (fireEvents) {
        ValueChangeEvent.fireIfNotEqual(this, old, this.value);
      }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
      return handlers.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
      handlers.fireEvent(event);
    }
  }
}
