package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import io.instanto.teavm.module.ScriptModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.*;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class LoadingAndHistoryRegressionTest {
  @Test
  public void formCreatesAnIsolatedSubmitTarget() {
    FormPanel form = new FormPanel();
    RootPanel.get().add(form);
    interceptSubmit(form.getElement().unwrap());
    try {
      form.submit();
      assertFalse(
          "default form submission must target its hidden iframe", form.getTarget().isEmpty());
    } finally {
      form.removeFromParent();
    }
  }

  @JSBody(
      params = "form",
      script = "form.reviewSubmits=0;form.submit=function(){form.reviewSubmits++;};")
  static native void interceptSubmit(JSObject form);

  @Test
  public void formCancellationCompletionAndReattachmentReleaseOwnedFrame() throws Exception {
    FormPanel form = new FormPanel();
    RootPanel.get().add(form);
    interceptSubmit(form.getElement().unwrap());
    int[] complete = {0};
    form.addSubmitCompleteHandler(
        e -> {
          assertTrue(e.getResults().contains("response"));
          complete[0]++;
        });
    try {
      var cancel = form.addSubmitHandler(e -> e.cancel());
      form.submit();
      assertEquals(0, submittedCount(form.getElement().unwrap()));
      cancel.removeHandler();
      form.submit();
      assertEquals(1, submittedCount(form.getElement().unwrap()));
      setFrameResponse(form.getElement().unwrap());
      Thread.sleep(100);
      assertEquals(1, complete[0]);
      form.removeFromParent();
      assertEquals(0, form.getElement().getElementsByTagName("iframe").getLength());
      RootPanel.get().add(form);
      assertEquals(1, form.getElement().getElementsByTagName("iframe").getLength());
    } finally {
      form.removeFromParent();
    }
  }

  @JSBody(params = "form", script = "return form.reviewSubmits || 0;")
  static native int submittedCount(JSObject form);

  @JSBody(params = "form", script = "form.querySelector('iframe').srcdoc='<body>response</body>';")
  static native void setFrameResponse(JSObject form);

  @Test
  public void historyFalseSuppressesNotification() throws InterruptedException {
    String previous = History.getToken();
    int[] events = {0};
    var registration = History.addValueChangeHandler(e -> events[0]++);
    try {
      History.newItem("review-silent-" + System.currentTimeMillis(), false);
      Thread.sleep(100);
      assertEquals("issueEvent=false must suppress hashchange notification", 0, events[0]);
    } finally {
      registration.removeHandler();
      History.newItem(previous, false);
    }
  }

  @Test
  public void alreadyLoadedScriptCanCompleteANewModule() throws InterruptedException {
    String src = "data:text/javascript,void(0)";
    JSObject script = loadScript(src);
    try {
      Thread.sleep(100);
      assertTrue("fixture script must finish loading", scriptLoaded(script));
      boolean[] completed = {false};
      ScriptModule.named("review-existing").script(src).whenReady(() -> completed[0] = true);
      Thread.sleep(100);
      assertTrue("existing loaded script must not leave module waiting forever", completed[0]);
    } finally {
      removeScript(script);
    }
  }

  @JSBody(
      params = "src",
      script =
          "var s=document.createElement('script');s.onload=function(){s.reviewLoaded=true;};s.src=src;document.head.appendChild(s);return"
              + " s;")
  static native JSObject loadScript(String src);

  @JSBody(params = "script", script = "return script.reviewLoaded === true;")
  static native boolean scriptLoaded(JSObject script);

  @JSBody(params = "script", script = "script.remove();")
  static native void removeScript(JSObject script);
}
