package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class GeneratedResourceTest {
  @Test
  public void generatedBundlesCacheResourcesAndPreserveData() {
    GeneratedResources.Bundle bundle = GWT.create(GeneratedResources.Bundle.class);
    assertSame(bundle.text(), bundle.text());
    assertSame(bundle.css(), bundle.css());
    assertSame(bundle.image(), bundle.image());
    assertSame(bundle.data().getSafeUri(), bundle.data().getSafeUri());
    assertEquals("resource text with \"quotes\" and two  spaces\n", bundle.text().getText());
    assertEquals(1, bundle.image().getWidth());
    assertEquals(1, bundle.image().getHeight());
    assertTrue(bundle.image().getSafeUri().asString().startsWith("data:image/png;base64,"));
    assertTrue(bundle.css().getText().contains("data:image/png;base64,"));
    assertTrue(bundle.css().getText().contains("content: \".button  literal\""));
    assertTrue(bundle.css().getText().contains("." + bundle.css().button() + ":hover"));
    assertEquals("external", bundle.css().external());
    assertTrue(bundle.css().ensureInjected());
    assertFalse(bundle.css().ensureInjected());
  }

  @Test
  public void safeHtmlTemplatesEscapeTextAttributesAndUnsafeUris() {
    GeneratedResources.Templates templates = GWT.create(GeneratedResources.Templates.class);
    String value =
        templates
            .link(
                "javascript:alert(1)",
                "\"<&",
                "<script>",
                SafeHtmlUtils.fromSafeConstant("<b>safe</b>"))
            .asString();
    assertEquals("<a href=\"#\" title=\"&quot;&lt;&amp;\">&lt;script&gt;<b>safe</b></a>", value);
    assertTrue(
        templates
            .link("https://example.test/?a=1&b=2", "", "", SafeHtmlUtils.EMPTY_SAFE_HTML)
            .asString()
            .contains("?a=1&amp;b=2"));
  }

  @Test
  public void generatedUiBinderUsesFactoryTypedStyleAndBundleChain() {
    GeneratedView first = new GeneratedView(), second = new GeneratedView();
    first.bind();
    second.bind();
    assertEquals(1, first.constructions);
    assertEquals("resource text with \"quotes\" and two  spaces\n", first.label.getText());
    assertEquals(first.style.local(), first.label.getStyleName());
    assertSame(first.style, second.style);
    assertFalse(first.style.ensureInjected());
  }
}
