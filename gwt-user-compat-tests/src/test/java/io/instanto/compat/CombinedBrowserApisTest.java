package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.dom.client.Document;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class CombinedBrowserApisTest {
  @Test
  public void bothLibrariesShareTheNativeDocument() {
    var legacy = Document.get().createDivElement();
    legacy.setId("combined-compat-contract");
    Document.get().getBody().appendChild(legacy);
    try {
      var elemental = DomGlobal.document.getElementById("combined-compat-contract");
      assertNotNull(elemental);
      elemental.setAttribute("data-round-trip", "working");
      assertEquals("working", legacy.getAttribute("data-round-trip"));
      var properties = Js.asPropertyMap(elemental);
      assertEquals("combined-compat-contract", Js.asString(properties.get("id")));
    } finally {
      legacy.removeFromParent();
    }
  }
}
